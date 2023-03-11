package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Recipe createNewRecipe(Recipe recipe) {
        recipe.setOwner(principalAccess.getUser());
        return recipeRepository.save(recipe);
    }

    public Recipe updateRecipe(Recipe recipe) {
        Recipe existing = recipeRepository.getReferenceById(recipe.getId());
        if (!existing.getOwner().equals(principalAccess.getUser())) {
            throw new RuntimeException("You can't update other people's recipes.");
        }
        return recipeRepository.save(recipe);
    }

    public Optional<Recipe> findRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public void deleteRecipeById(Long id) {
        Recipe r = recipeRepository.getReferenceById(id);
        planService.severLibraryLinks(r);
        recipeRepository.delete(r);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId) {
        planService.addRecipe(planId,
                              recipeRepository.findById(recipeId).get());
    }

    public SearchResponse<Recipe> searchRecipes(String scope,
                                                String filter,
                                                int offset,
                                                int limit) {
        return searchRecipes(LibrarySearchScope.valueOf(scope.toUpperCase()),
                             filter,
                             offset,
                             limit);
    }

    public SearchResponse<Recipe> searchRecipes(LibrarySearchScope scope,
                                                String filter,
                                                int offset,
                                                int limit) {
        return recipeRepository.searchRecipes(
                LibrarySearchRequest.builder()
                        .user(principalAccess.getUser())
                        .scope(scope)
                        .filter(filter)
                        .offset(offset)
                        .limit(limit)
                        .build());
    }

}
