package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.services.events.TaskStatusEvent;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NumberUtils;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    IngredientService ingredientService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTmpl;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    @Deprecated
    private ItemService itemService;

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
        //noinspection rawtypes
        for (IngredientRef ref : agg.assembleRawIngredientRefs()) {
            String raw = ref.getRaw().trim();
            if (raw.isEmpty()) continue;
            list.addSubtask(new Task(raw));
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
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

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToShoppingList(Long recipeId, Long listId) {
        Recipe r = recipeRepository.findById(recipeId).get();
        sendToShoppingList(r, taskRepository.getOne(listId));
    }

    private void sendToPlan(AggregateIngredient r, Task rTask) {
        r.getIngredients().forEach(ir ->
                sendToPlan(ir, rTask));
    }

    private void sendToPlan(IngredientRef<?> ir, Task rTask) {
        Task t = new Task(ir.getRaw(), ir.getQuantity(), ir.getIngredient(), ir.getPreparation());
        rTask.addSubtask(t);
        if (ir.getIngredient() instanceof AggregateIngredient) {
            sendToPlan((AggregateIngredient) ir.getIngredient(), t);
        }
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId) {
        Recipe r = recipeRepository.findById(recipeId).get();
        Task rTask = new Task(r.getName());
        taskRepository.getOne(planId).addSubtask(rTask);
        sendToPlan(r, rTask);
    }

    private void taskCompleted(Long id) {
        System.out.println("YO! WOO! Task #" + id + " was completed!");
        entityManager.createQuery("select l\n" +
                "from ShoppingList l\n" +
                "    join l.items it\n" +
                "    join it.task t\n" +
                "where t.id = :taskId", ShoppingList.class)
        .setParameter("taskId", id)
        .getResultList()
        .forEach(l -> {
            System.out.println("Yo! Woo! Shopping List #" + l.getId() + " had an item completed!");
            l.taskCompleted(id);
        });
    }

    @EventListener
    @Order(1) // this needs to first so the FK isn't wiped by the DELETE
    public void taskStatusChanged(TaskStatusEvent e) {
        if (TaskStatus.COMPLETED.equals(e.getStatus())) {
            taskCompleted(e.getId());
        }
    }

    public void recordDissection(RawIngredientDissection dissection) {
        // fail fast on unparseable quantity
        String qText = dissection.getQuantityText();
        Double quantity = NumberUtils.parseNumber(qText);
        if (qText != null && quantity == null) {
            throw new RuntimeException("Quantity '" + quantity + "' cannot be parsed.");
        }

        // lets go!
        Ingredient ingredient = ingredientService.ensureIngredientByName(
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

    /**
     * I am hand off to {@link ItemService#recognizeItem(String, int)} with no
     * additional processing and exist purely to ease wiring at the controller
     * level. I should not be used in new code.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public RecognizedItem recognizeItem(String raw, int cursor) {
        return itemService.recognizeItem(raw, cursor);
    }
}
