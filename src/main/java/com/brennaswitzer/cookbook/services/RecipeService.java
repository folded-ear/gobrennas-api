package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public Recipe saveOrUpdateRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public Recipe findRecipeById(Long id) {
        return recipeRepository.findRecipeById(id);
    }

    public Iterable<Recipe> findAllRecipes() {
        return recipeRepository.findAll();
    }

    public void deleteRecipeById(Long id) {
        Recipe recipe = recipeRepository.findRecipeById(id);
        recipeRepository.delete(recipe);
    }

}
