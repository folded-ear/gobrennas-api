package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.*;

import javax.persistence.EntityManager;

public class RecipeBox {

    public final UnitOfMeasure tsp,
            cup,
            tbsp,
            lbs;

    public final PantryItem
            salt;

    public final Recipe
            friedChicken,
            pizza,
            pizzaCrust,
            pizzaSauce;

    public RecipeBox() {
        tsp = new UnitOfMeasure("tsp");
        cup = new UnitOfMeasure("c")
                .withAlias("cup");
        tbsp = new UnitOfMeasure("Tbsp");
        lbs = new UnitOfMeasure("lbs");

        salt = new PantryItem("salt");

        friedChicken = new Recipe("Fried Chicken");
        friedChicken.addIngredient(Quantity.count(2), new PantryItem("egg"), "shelled");
        friedChicken.addIngredient(new PantryItem("chicken"), "deboned");
        friedChicken.addIngredient(new PantryItem("chicken thigh"), "cut");

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
        pizzaCrust.addIngredient(tbsp.quantity(1), new PantryItem("oil"));
        pizzaSauce.addIngredient(tsp.quantity(0.5), salt);

        pizza = new Recipe("Pizza");
        pizza.addRawIngredient("pepperoni");
        pizza.addIngredient(new UnitOfMeasure("oz").quantity(8), pizzaSauce);
        pizza.addIngredient(Quantity.ONE, pizzaCrust);


    }

    public void persist(EntityManager entityManager, User owner) {
        persist(entityManager, owner, friedChicken);
        persist(entityManager, owner, pizza);
        persist(entityManager, owner, pizzaCrust);
        persist(entityManager, owner, pizzaSauce);
    }

    private void persist(EntityManager entityManager, User owner, Recipe recipe) {
        recipe.setOwner(owner);
        entityManager.persist(recipe);
        recipe.getIngredients().forEach(ref -> {
            if (ref.hasQuantity()) {
                Quantity q = ref.getQuantity();
                if (q.hasUnits()) {
                    entityManager.persist(q.getUnits());
                }
            }
            if (!ref.hasIngredient()) return;
            if (ref.getIngredient() instanceof Recipe) {
                persist(entityManager, owner, (Recipe) ref.getIngredient());
            } else {
                entityManager.persist(ref.getIngredient());
            }
        });
    }

}
