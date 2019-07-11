package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.TaskRepository;
import com.brennaswitzer.cookbook.services.events.TaskCompletedEvent;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Optional;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EntityManager entityManager;

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

    public void assembleShoppingList(
            AggregateIngredient agg,
            Task list,
            boolean withHeading
    ) {
        ShoppingList l = new ShoppingList();
        l.addAllPantryItems(agg);
        entityManager.persist(l);
        if (withHeading) {
            l.createTasks(agg.getName(), list);
        } else {
            l.createTasks(list);
        }
        // also do any raw ingredients
        for (IngredientRef ref : agg.assembleRawIngredientRefs()) {
            String raw = ref.getRaw().trim();
            if (raw.isEmpty()) continue;
            list.addSubtask(new Task(raw));
        }
    }

    public void assembleShoppingList(Long recipeId, Long listId, boolean withHeading) {
        assembleShoppingList(
                recipeRepository.findById(recipeId).get(),
                taskRepository.getOne(listId),
                withHeading
        );
    }

    @EventListener
    public void taskCompleted(TaskCompletedEvent e) {
        System.out.println("YO! WOO! Task #" + e.getId() + " was completed!");
        entityManager.createQuery("select l\n" +
                "from ShoppingList l\n" +
                "    join l.items it\n" +
                "    join it.task t\n" +
                "where t.id = :taskId", ShoppingList.class)
        .setParameter("taskId", e.getId())
        .getResultList()
        .forEach(l -> {
            System.out.println("Yo! Woo! Shopping List #" + l.getId() + " had an item completed!");
            l.taskCompleted(e.getId());
        });
    }

}
