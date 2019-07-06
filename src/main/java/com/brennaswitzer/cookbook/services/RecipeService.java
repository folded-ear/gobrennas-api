package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Recipe saveOrUpdateRecipe(Recipe recipe) {
        recipe.setOwner(principalAccess.getUser());
        return recipeRepository.save(recipe);
    }

    public Optional<Recipe> findRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Iterable<Recipe> findAllRecipes() {
        return recipeRepository.findByOwner(principalAccess.getUser());
    }

    public void deleteRecipeById(Long id) {
        recipeRepository.deleteById(id);
    }

    private void saveSubtask(Task parent, String name) {
        Task t = new Task(name);
        parent.addSubtask(t);
        taskRepository.save(t);
    }

    public void addPurchasableSchmankiesToList(
            AggregateIngredient agg,
            Task list,
            boolean withHeading
    ) {
        if (withHeading) {
            saveSubtask(list, agg.getName() + ":");
        }
        Map<PantryItem, IngredientRef<PantryItem>> grouped = new LinkedHashMap<>();
        for (IngredientRef<PantryItem> ref : agg.getPurchasableSchmankies()) {
            PantryItem ingredient = ref.getIngredient();
            if (grouped.containsKey(ingredient)) {
                grouped.put(ingredient, grouped.get(ingredient).plus(ref));
            } else {
                grouped.put(ingredient, ref);
            }
        }
        for (IngredientRef ref : grouped.values()) {
            StringBuilder sb = new StringBuilder().append(ref.getIngredient().getName());
            if (ref.getQuantity() != null && ! ref.getQuantity().isEmpty()) {
                sb.append(" (").append(ref.getQuantity()).append(')');
            }
            saveSubtask(list, sb.toString());
        }
    }

    public void addPurchasableSchmankiesToList(Long recipeId, Long listId, boolean withHeading) {
        addPurchasableSchmankiesToList(
                recipeRepository.findById(recipeId).get(),
                taskRepository.getOne(listId),
                withHeading
        );
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
            if (line.isEmpty()) continue;
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
