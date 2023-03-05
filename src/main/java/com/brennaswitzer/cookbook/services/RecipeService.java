package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    public void deleteRecipeById(Long id) {
        Recipe r = recipeRepository.getOne(id);
        planService.severLibraryLinks(r);
        recipeRepository.delete(r);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId) {
        planService.addRecipe(planId,
                recipeRepository.findById(recipeId).get());
    }

    public Slice<Recipe> searchRecipes(String scope, String filter, Pageable pageable) {
        User user = principalAccess.getUser();
        if ("everyone".equals(scope)) {
            return recipeRepository.searchRecipes(user, filter, pageable);
        } else {
            return recipeRepository.searchRecipesByOwner(
                    user,
                    Collections.singletonList(user),
                    filter,
                    pageable);
        }
    }

}
