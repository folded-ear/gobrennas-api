package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
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
    private PlanService planService;

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
        return recipeRepository.findAllByTermContainingIgnoreCase(filter);
    }

    public Iterable<Recipe> findRecipeByNameAndOwner(String filter) {
        return recipeRepository.findAllByOwnerAndTermContainingIgnoreCase(principalAccess.getUser(),filter);
    }

    public void deleteRecipeById(Long id) {
        recipeRepository.deleteById(id);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId) {
        planService.addRecipe(planId,
                recipeRepository.findById(recipeId).get());
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
