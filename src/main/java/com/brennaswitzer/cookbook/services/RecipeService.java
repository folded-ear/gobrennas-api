package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    public Recipe saveOrUpdateRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    public Optional<Recipe> findRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Iterable<Recipe> findAllRecipes() {
        return recipeRepository.findAll();
    }

    public void deleteRecipeById(Long id) {
        recipeRepository.deleteById(id);
    }

    public void addRawIngredientsToList(
            Recipe recipe,
            TaskList list,
            boolean withHeading
    ) {
        if (withHeading) {
            list.addSubtask(new Task(recipe.getTitle() + ":"));
        }
        for (String line : recipe.getRawIngredients().split("\n")) {
            line = line.trim();
            if (line.length() == 0) continue;
            list.addSubtask(new Task(line));
        }
    }

}
