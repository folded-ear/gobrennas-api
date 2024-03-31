package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.domain.User;
import jakarta.persistence.EntityManager;

public class RecipeBox {

    public final UnitOfMeasure
            tsp,
            cup,
            tbsp,
            lbs;

    public final PantryItem
            egg,
            flour,
            oil,
            salt,
            sugar,
            water,
            yeast;

    public final Label
            dinner,
            makeAhead;

    public final Recipe
            friedChicken,
            pizza,
            pizzaCrust,
            pizzaSauce,
            spanishAppleCake;

    public RecipeBox() {
        tsp = new UnitOfMeasure("tsp");
        cup = new UnitOfMeasure("c")
                .withAlias("cup");
        tbsp = new UnitOfMeasure("Tbsp");
        cup.addConversion(tbsp, 16);
        lbs = new UnitOfMeasure("lbs");

        egg = new PantryItem("egg");
        flour = new PantryItem("flour");
        oil = new PantryItem("oil");
        salt = new PantryItem("salt")
                .withSynonym("nacl");
        sugar = new PantryItem("sugar");
        water = new PantryItem("water");
        yeast = new PantryItem("yeast");

        dinner = new Label("dinner");
        makeAhead = new Label("make ahead");

        friedChicken = new Recipe("Fried Chicken");
        friedChicken.addIngredient(Quantity.count(2), egg, "shelled");
        friedChicken.addIngredient(new PantryItem("chicken"), "deboned");
        friedChicken.addIngredient(new PantryItem("chicken thigh"), "cut");
        friedChicken.addLabel(dinner);

        pizzaSauce = new Recipe("Pizza Sauce");
        pizzaSauce.addIngredient(lbs.quantity(1), new PantryItem("fresh tomatoes"), "seeded and crushed");
        pizzaSauce.addIngredient(new UnitOfMeasure("(6 oz) can").quantity(1), new PantryItem("tomato paste"));
        pizzaSauce.addIngredient(new PantryItem("italian seasoning"));
        pizzaSauce.addIngredient(tsp.quantity(1), salt);
        pizzaSauce.addLabel(makeAhead);

        pizzaCrust = new Recipe("Pizza Crust");
        pizzaCrust.setDirections("knead it a lot!");
        pizzaCrust.addIngredient(cup.quantity(2), flour);
        pizzaCrust.addIngredient(cup.quantity(1), water);
        pizzaCrust.addIngredient(new UnitOfMeasure("packet").quantity(1), yeast);
        pizzaCrust.addIngredient(tbsp.quantity(1), sugar);
        pizzaCrust.addIngredient(tbsp.quantity(1), oil);
        pizzaCrust.addIngredient(tsp.quantity(0.5), salt);
        pizzaCrust.addLabel(makeAhead);

        pizza = new Recipe("Pizza");
        pizza.addRawIngredient("pepperoni");
        pizza.addIngredient(new UnitOfMeasure("oz").quantity(8), pizzaSauce);
        pizza.addIngredient(Quantity.ONE, pizzaCrust);
        pizza.addLabel(dinner);

        spanishAppleCake = new Recipe("Spanish Apple Cake");
        spanishAppleCake.addIngredient(cup.quantity(2), new PantryItem("apple"));
    }

    public void persist(EntityManager entityManager, User owner) {
        entityManager.persist(dinner);
        entityManager.persist(makeAhead);
        persist(entityManager, owner, friedChicken);
        persist(entityManager, owner, pizza);
        persist(entityManager, owner, pizzaCrust);
        persist(entityManager, owner, pizzaSauce);
        persist(entityManager, owner, spanishAppleCake);
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
