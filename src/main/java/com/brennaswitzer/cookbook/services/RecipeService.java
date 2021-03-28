package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecognizedItem;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    /**
     * I am hand off to {@link ItemService#recognizeItem(String, int)} with no
     * additional processing and exist purely to ease wiring at the controller
     * level. I should not be used in new code.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public RecognizedItem recognizeItem(String raw, int cursor) { // todo: go away
        return itemService.recognizeItem(raw, cursor);
    }

    public Slice<Recipe> searchRecipes(String scope, String filter, Pageable pageable) {
        if ("everyone".equals(scope)) {
            return recipeRepository.searchRecipes(filter, pageable);
        } else {
            return recipeRepository.searchRecipesByOwner(
                    Collections.singletonList(principalAccess.getUser()),
                    filter,
                    pageable);
        }
    }

}
