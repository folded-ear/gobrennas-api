package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;

import javax.persistence.EntityManager;

public class RecipeBox {

    public final PantryItem
            salt;

    public final Recipe
            friedChicken,
            pizza,
            pizzaCrust,
            pizzaSauce;

    public RecipeBox() {
        salt = new PantryItem("salt");

        friedChicken = new Recipe("Fried Chicken");
        friedChicken.addIngredient("2", new PantryItem("egg"), "shelled");
        friedChicken.addIngredient(new PantryItem("chicken"), "deboned");

        pizzaSauce = new Recipe("Pizza Sauce");
        pizzaSauce.addIngredient("1 lbs", new PantryItem("fresh tomatoes"), "seeded and crushed");
        pizzaSauce.addIngredient("1 (6 oz) can", new PantryItem("tomato paste"));
        pizzaSauce.addIngredient(new PantryItem("italian seasoning"));
        pizzaSauce.addIngredient("1 tsp", salt);

        pizzaCrust = new Recipe("Pizza Crust");
        pizzaCrust.addIngredient("2 c", new PantryItem("flour"));
        pizzaCrust.addIngredient("1 c", new PantryItem("water"));
        pizzaCrust.addIngredient("1 packet", new PantryItem("yeast"));
        pizzaCrust.addIngredient("1 Tbsp", new PantryItem("sugar"));
        pizzaSauce.addIngredient("0.5 tsp", salt);

        pizza = new Recipe("Pizza");
        pizza.addIngredient("4 oz", new PantryItem("pepperoni"));
        pizza.addIngredient("8 oz", pizzaSauce);
        pizza.addIngredient("1", pizzaCrust);
    }

    public void persist(EntityManager entityManager) {
        persist(entityManager, friedChicken);
        persist(entityManager, pizza);
        persist(entityManager, pizzaCrust);
        persist(entityManager, pizzaSauce);
    }

    private void persist(EntityManager entityManager, Recipe recipe) {
        entityManager.persist(recipe);
        recipe.getPurchasableSchmankies().forEach(ref ->
                entityManager.persist(ref.getIngredient()));
    }

}
