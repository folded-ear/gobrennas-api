package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedElement;
import com.brennaswitzer.cookbook.repositories.IngredientRepository;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private IngredientRepository ingredientRepository;

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

    public Iterable<Recipe> findRecipeByName(String filter) {
        return recipeRepository.findAllByTermIgnoreCase(filter);
    }

    public Iterable<Recipe> findRecipeByNameAndOwner(String filter) {
        return recipeRepository.findAllByOwnerAndTermIgnoreCase(principalAccess.getUser(),filter);
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
        // if no cursor location is specified, assume it's at the end
        return recognizeElement(raw, raw.length());
    }

    public RecognizedElement recognizeElement(String raw, int cursor) {
        if (raw == null) return null;
        if (raw.trim().isEmpty()) return null;
        RecognizedElement el = new RecognizedElement(raw, cursor);
        RawIngredientDissection d = RawUtils.dissect(raw);
        RawIngredientDissection.Section secAmount = d.getQuantity();
        if (secAmount != null) {
            // there's an amount
            el.withRange(new RecognizedElement.Range(
                    secAmount.getStart(),
                    secAmount.getEnd(),
                    RecognizedElement.Type.AMOUNT
            ).withValue(NumberUtils.parseNumber(secAmount.getText())));
        }
        RawIngredientDissection.Section secUnit = d.getUnits();
        if (secUnit != null) {
            // there's an explicit unit
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
        }
        RawIngredientDissection.Section secName = d.getName();
        int idxNameStart = -1;
        if (secName != null) {
            // there's an explicit name
            Optional<? extends Ingredient> oing = findIngredientByName(
                    secName.getText());
            idxNameStart = secName.getStart();
            el.withRange(new RecognizedElement.Range(
                    secName.getStart(),
                    secName.getEnd(),
                    oing.isPresent()
                            ? RecognizedElement.Type.ITEM
                            : RecognizedElement.Type.NEW_ITEM,
                    oing.map(Ingredient::getId).orElse(null)
            ));
        } else if (!raw.contains("\"")) {
            // no name, so see if there's an implicit one
            for (RecognizedElement.Range r : el.unrecognizedWords()) {
                Optional<? extends Ingredient> oing = findIngredientByName(
                        raw.substring(r.getStart(), r.getEnd()));
                if (!oing.isPresent()) continue;
                idxNameStart = r.getStart();
                el.withRange(r.of(RecognizedElement.Type.ITEM).withValue(oing.get().getId()));
                break;
            }
        }
        if (secAmount != null && secUnit == null && !raw.contains("_")) {
            // there's an amount, but no explicit unit, so see if there's an implicit one
            for (RecognizedElement.Range r : el.unrecognizedWords()) {
                // unit must precede name, so abort if we get there
                if (idxNameStart >= 0 && idxNameStart < r.getStart()) break;
                Optional<UnitOfMeasure> ouom = UnitOfMeasure.find(
                        entityManager,
                        raw.substring(r.getStart(), r.getEnd()));
                if (!ouom.isPresent()) continue;
                el.withRange(r.of(RecognizedElement.Type.UNIT).withValue(ouom.get().getId()));
                break;
            }
        }
        if (idxNameStart < 0) { // there's no name, explicit or implicit
            // based on cursor position, see if we can suggest any names
            // start with looking backwards for a quote
            int start = raw.lastIndexOf('"', el.getCursor());
            boolean hasQuote = true;
            boolean hasSpace = false;
            if (start < 0) { // look backwards for a space
                start = raw.lastIndexOf(' ', el.getCursor());
                hasQuote = false;
                hasSpace = true;
            }
            if (start < 0) { // whole prefix, i guess
                start = 0;
                hasQuote = false;
                hasSpace = false;
            }
            int replaceStart = hasSpace ? start + 1 : start;
            String search = raw.substring(hasQuote ? replaceStart + 1 : replaceStart, el.getCursor())
                    .trim()
                    .toLowerCase();
            if (!search.isEmpty()) {
                Iterable<Ingredient> matches = ingredientRepository.findByNameContains(search);
                StreamSupport.stream(matches.spliterator(), false)
                        .limit(10)
                        .forEach(i -> {
                            el.withSuggestion(new RecognizedElement.Suggestion(
                                    i.getName(),
                                    new RecognizedElement.Range(
                                            replaceStart,
                                            el.getCursor(),
                                            RecognizedElement.Type.ITEM,
                                            i.getId()
                                    )
                            ));
                        });
            }
        }
        return el;
    }
}
