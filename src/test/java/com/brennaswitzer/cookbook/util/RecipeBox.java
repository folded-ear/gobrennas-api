package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;

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
        UnitOfMeasure tsp = new UnitOfMeasure("tsp"),
                cup = new UnitOfMeasure("c"),
                tbsp = new UnitOfMeasure("Tbsp"),
                lbs = new UnitOfMeasure("lbs");

        salt = new PantryItem("salt");

        friedChicken = new Recipe("Fried Chicken");
        friedChicken.addIngredient(Quantity.count(2), new PantryItem("egg"), "shelled");
        friedChicken.addIngredient(new PantryItem("chicken"), "deboned");

        pizzaSauce = new Recipe("Pizza Sauce");
        pizzaSauce.addIngredient(lbs.quantity(1), new PantryItem("fresh tomatoes"), "seeded and crushed");
        pizzaSauce.addIngredient(new UnitOfMeasure("(6 oz) can").quantity(1), new PantryItem("tomato paste"));
        pizzaSauce.addIngredient(new PantryItem("italian seasoning"));
        pizzaSauce.addIngredient(tsp.quantity(1), salt);

        pizzaCrust = new Recipe("Pizza Crust");
        pizzaCrust.addIngredient(cup.quantity(2), new PantryItem("flour"));
        pizzaCrust.addIngredient(cup.quantity(1), new PantryItem("water"));
        pizzaCrust.addIngredient(new UnitOfMeasure("packet").quantity(1), new PantryItem("yeast"));
        pizzaCrust.addIngredient(tbsp.quantity(1), new PantryItem("sugar"));
        pizzaSauce.addIngredient(tsp.quantity(0.5), salt);

        pizza = new Recipe("Pizza");
        pizza.addRawIngredient("pepperoni");
        pizza.addIngredient(new UnitOfMeasure("oz").quantity(8), pizzaSauce);
        pizza.addIngredient(Quantity.ONE, pizzaCrust);
    }

    public void persist(EntityManager entityManager) {
        persist(entityManager, friedChicken);
        persist(entityManager, pizza);
        persist(entityManager, pizzaCrust);
        persist(entityManager, pizzaSauce);
    }

    private void persist(EntityManager entityManager, Recipe recipe) {
        entityManager.persist(recipe);
        recipe.assemblePantryItemRefs().forEach(ref ->
                entityManager.persist(ref.getIngredient()));
    }

}
