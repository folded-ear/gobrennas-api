package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskListRepository;
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
    private TaskListRepository taskListRepository;

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

    public void addPurchaseableSchmankiesToList(
            AggregateIngredient agg,
            TaskList list,
            boolean withHeading
    ) {
        if (withHeading) {
            list.addSubtask(new Task(agg.getName() + ":"));
        }
        for (IngredientRef ref : agg.getPurchasableSchmankies()) {
            list.addSubtask(new Task(ref.toString()));
        }
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

    public void addRawIngredientsToList(Long recipeId, Long listId, boolean withHeading) {
        addRawIngredientsToList(
                recipeRepository.findById(recipeId).get(),
                taskListRepository.getOne(listId),
                withHeading
        );
    }

}
