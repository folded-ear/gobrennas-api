package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
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
    private TaskRepository taskRepository;

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

    private void saveSubtask(Task parent, String name) {
        Task t = new Task(name);
        parent.addSubtask(t);
        taskRepository.save(t);
    }

    public void addPurchaseableSchmankiesToList(
            AggregateIngredient agg,
            Task list,
            boolean withHeading
    ) {
        if (withHeading) {
            saveSubtask(list, agg.getName() + ":");
        }
        for (IngredientRef ref : agg.getPurchasableSchmankies()) {
            saveSubtask(list, ref.toString(false));
        }
    }

    public void addRawIngredientsToList(
            Recipe recipe,
            Task list,
            boolean withHeading
    ) {
        if (withHeading) {
            saveSubtask(list, recipe.getName() + ":");
        }
        for (String line : recipe.getRawIngredients().split("\n")) {
            line = line.trim();
            if (line.length() == 0) continue;
            saveSubtask(list, line);
        }
    }

    public void addRawIngredientsToList(Long recipeId, Long listId, boolean withHeading) {
        addRawIngredientsToList(
                recipeRepository.findById(recipeId).get(),
                taskRepository.getOne(listId),
                withHeading
        );
    }

}
