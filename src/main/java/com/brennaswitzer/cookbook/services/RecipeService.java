package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.NumberUtils;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

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
        Task rTask = new Task(r.getName(), r);
        taskRepository.getOne(planId).addSubtask(rTask);
        sendToPlan(r, rTask);
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
    @Deprecated
    public RecognizedItem recognizeItem(String raw, int cursor) {
        return itemService.recognizeItem(raw, cursor);
    }
}
