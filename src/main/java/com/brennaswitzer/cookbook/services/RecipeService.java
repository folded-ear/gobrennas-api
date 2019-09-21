package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedElement;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.services.events.TaskCompletedEvent;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NumberUtils;
import com.brennaswitzer.cookbook.util.RawUtils;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTmpl;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Recipe createNewRecipe(Recipe recipe) {
        recipe.setOwner(principalAccess.getUser());
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(Recipe recipe) {
        Recipe existing = recipeRepository.getOne(recipe.getId());
        if (!existing.getOwner().equals(principalAccess.getUser())) {
            throw new RuntimeException("You can't update other people's recipes.");
        }
        return recipeRepository.save(recipe);
    }

    public Optional<Recipe> findRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> findMyRecipes() {
        return recipeRepository.findByOwner(principalAccess.getUser());
    }

    /** use {@link #findMyRecipes} instead */
    @Deprecated
    public List<Recipe> findAllRecipes() {
        return findMyRecipes();
    }

    public List<Recipe> findEveryonesRecipes() {
        return recipeRepository.findAll();
    }

    public void deleteRecipeById(Long id) {
        recipeRepository.deleteById(id);
    }

    public void assembleShoppingList(
            AggregateIngredient agg,
            Task list,
            boolean withHeading
    ) {
        ShoppingList l = new ShoppingList();
        l.addAllPantryItems(agg);
        // todo: make this user specific
        l.resetItemOrder(new MagicalListItemStatsComparator(jdbcTmpl));
        entityManager.persist(l);
        if (withHeading) {
            l.createTasks(agg.getName(), list);
        } else {
            l.createTasks(list);
        }
        // also do any raw ingredients
        for (IngredientRef ref : agg.assembleRawIngredientRefs()) {
            String raw = ref.getRaw().trim();
            if (raw.isEmpty()) continue;
            list.addSubtask(new Task(raw));
        }
    }

    public void assembleShoppingList(Long recipeId, List<Long> additionalRecipeIds, Long listId, boolean withHeading) {
        Recipe r;
        if (additionalRecipeIds == null || additionalRecipeIds.isEmpty()) {
            // simple case - a single recipe
            r = recipeRepository.findById(recipeId).get();
        } else {
            // synthetic aggregate recipe
            r = new Recipe("Shopping List");
            r.addIngredient(recipeRepository.findById(recipeId).get());
            additionalRecipeIds.forEach(id ->
                    r.addIngredient(recipeRepository.findById(id).get()));
        }
        assembleShoppingList(r, taskRepository.getOne(listId), withHeading);
    }

    private void sendToShoppingList(Recipe r, Task list) {
        class Pair {
            private final double factor;
            private final Recipe recipe;

            private Pair(double factor, Recipe r) {
                this.factor = factor;
                this.recipe = r;
            }
        }
        Queue<Pair> queue = new LinkedList<>();
        queue.add(new Pair(1, r));
        while (!queue.isEmpty()) {
            Pair p = queue.remove();
            ShoppingList l = new ShoppingList();
            p.recipe.getIngredients().forEach(ref -> {
                if (!ref.hasIngredient()) return; // do it later...
                Ingredient ing = ref.getIngredient();
                Quantity q = ref.getQuantity();
                if (ing instanceof Recipe) {
                    queue.add(new Pair(
                            p.factor * q.getQuantity(),
                            (Recipe) ing));
                } else {
                    l.addPantryItem(new IngredientRef<>(
                            q.times(p.factor),
                            (PantryItem) ing,
                            ref.getPreparation()
                    ));
                }
            });
            l.createTasks(p.recipe.getName(), list);
            p.recipe.getIngredients().forEach(ref -> {
                if (ref.hasIngredient()) return; // already did it
                String raw = ref.getRaw().trim();
                if (raw.isEmpty()) return;
                list.addSubtask(new Task(raw));
            });
        }
    }

    public void sendToShoppingList(Long recipeId, Long listId) {
        Recipe r = recipeRepository.findById(recipeId).get();
        sendToShoppingList(r, taskRepository.getOne(listId));
    }

    @EventListener
    public void taskCompleted(TaskCompletedEvent e) {
        System.out.println("YO! WOO! Task #" + e.getId() + " was completed!");
        entityManager.createQuery("select l\n" +
                "from ShoppingList l\n" +
                "    join l.items it\n" +
                "    join it.task t\n" +
                "where t.id = :taskId", ShoppingList.class)
        .setParameter("taskId", e.getId())
        .getResultList()
        .forEach(l -> {
            System.out.println("Yo! Woo! Shopping List #" + l.getId() + " had an item completed!");
            l.taskCompleted(e.getId());
        });
    }

    public void recordDissection(RawIngredientDissection dissection) {
        // fail fast on unparseable quantity
        String qText = dissection.getQuantityText();
        Double quantity = NumberUtils.parseNumber(qText);
        if (qText != null && quantity == null) {
            throw new RuntimeException("Quantity '" + quantity + "' cannot be parsed.");
        }

        // lets go!
        Ingredient ingredient = ensureIngredientByName(
                EnglishUtils.canonicalize(dissection.getNameText()));
        UnitOfMeasure uom = UnitOfMeasure.ensure(entityManager,
                EnglishUtils.canonicalize(dissection.getUnitsText()));
        // update raw refs w/ dissected parts
        // this is a kludge to bulk update all equivalent refs. :)
        entityManager.flush();
        jdbcTmpl.update("update recipe_ingredients\n" +
                        "set quantity = ?,\n" +
                        "    units_id = ?,\n" +
                        "    ingredient_id = ?,\n" +
                        "    preparation = ?\n" +
                        "where raw = ?",
                quantity,
                uom == null ? null : uom.getId(),
                ingredient.getId(),
                dissection.getPrep(),
                dissection.getRaw());

    }

    private Optional<? extends Ingredient> findIngredientByName(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        // see if there's a pantry item...
        Optional<PantryItem> pantryItem = pantryItemRepository.findOneByNameIgnoreCase(unpluralized);
        if (pantryItem.isPresent()) {
            return pantryItem;
        }
        // see if there's a recipe...
        Optional<Recipe> recipe = recipeRepository.findOneByOwnerAndNameIgnoreCase(principalAccess.getUser(), name);
        if (recipe.isPresent()) {
            return recipe;
        }
        return recipeRepository.findOneByOwnerAndNameIgnoreCase(principalAccess.getUser(), unpluralized);
    }

    private Ingredient ensureIngredientByName(String name) {
        Optional<? extends Ingredient> oing = findIngredientByName(name);
        if (oing.isPresent()) {
            return oing.get();
        }
        // make a new pantry item
        return pantryItemRepository.save(new PantryItem(EnglishUtils.unpluralize(name)));
    }

    public RecognizedElement recognizeElement(String raw) {
        if (raw == null) return null;
        if (raw.trim().isEmpty()) return null;
        RecognizedElement el = new RecognizedElement(raw);
        RawIngredientDissection d = RawUtils.dissect(raw);
        RawIngredientDissection.Section secAmount = d.getQuantity();
        if (secAmount != null) {
            el.withRange(new RecognizedElement.Range(
                    secAmount.getStart(),
                    secAmount.getEnd(),
                    RecognizedElement.Type.AMOUNT
            ).withValue(NumberUtils.parseNumber(secAmount.getText())));
        }
        RawIngredientDissection.Section secUnit = d.getUnits();
        if (secUnit != null) {
            Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                    entityManager,
                    secUnit.getText());
            el.withRange(new RecognizedElement.Range(
                    secUnit.getStart(),
                    secUnit.getEnd(),
                    ouom.isPresent()
                            ? RecognizedElement.Type.UNIT
                            : RecognizedElement.Type.NEW_UNIT,
                    ouom.map(UnitOfMeasure::getId).orElse(null)
            ));
        } else if (secAmount != null) {
            for (RecognizedElement.Range r : el.unrecognizedWords()) {
                Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                        entityManager,
                        raw.substring(r.getStart(), r.getEnd()));
                if (!ouom.isPresent()) continue;
                el.withRange(r.of(RecognizedElement.Type.UNIT).withValue(ouom.get().getId()));
            }
        }
        RawIngredientDissection.Section secName = d.getName();
        if (secName != null) {
            Optional<? extends Ingredient> oing = findIngredientByName(
                    secName.getText());
            el.withRange(new RecognizedElement.Range(
                    secName.getStart(),
                    secName.getEnd(),
                    oing.isPresent()
                            ? RecognizedElement.Type.ITEM
                            : RecognizedElement.Type.NEW_ITEM,
                    oing.map(Ingredient::getId).orElse(null)
            ));
        } else {
            Iterator<PantryItem> items = pantryItemRepository.findAll().iterator();
            for (RecognizedElement.Range r : el.unrecognizedWords()) {
                Optional<? extends Ingredient> oing = findIngredientByName(
                        raw.substring(r.getStart(), r.getEnd()));
                if (!oing.isPresent()) {
                    // todo: random suggestions aren't _that_ helpful...
                    if (items.hasNext()) {
                        PantryItem it = items.next();
                        el.withSuggestion(new RecognizedElement.Suggestion(
                                it.getName(),
                                r.of(RecognizedElement.Type.ITEM)
                                        .withValue(it.getId())
                        ));
                    }
                    continue;
                }
                el.withRange(r.of(RecognizedElement.Type.ITEM).withValue(oing.get().getId()));
            }
        }
        return el;
    }
}
