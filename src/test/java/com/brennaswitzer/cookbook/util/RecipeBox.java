package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;

public final class RecipeBox {

    public static final Recipe
            FRIED_CHICKEN,
            PIZZA,
            PIZZA_CRUST,
            PIZZA_SAUCE;

    static {
        FRIED_CHICKEN = new Recipe("Fried Chicken");
        FRIED_CHICKEN.addIngredient("2", new PantryItem("egg"), "shelled");
        FRIED_CHICKEN.addIngredient(new PantryItem("chicken"), "deboned");

        PIZZA_SAUCE = new Recipe("Pizza Sauce");
        PIZZA_SAUCE.addIngredient("1 lbs", new PantryItem("fresh tomatoes"), "seeded and crushed");
        PIZZA_SAUCE.addIngredient("1 (6 oz) can", new PantryItem("tomato paste"));
        PIZZA_SAUCE.addIngredient(new PantryItem("italian seasoning"));

        PIZZA_CRUST = new Recipe("Pizza Crust");
        PIZZA_CRUST.addIngredient("2 c", new PantryItem("flour"));
        PIZZA_CRUST.addIngredient("1 c", new PantryItem("water"));
        PIZZA_CRUST.addIngredient("1 packet", new PantryItem("yeast"));
        PIZZA_CRUST.addIngredient("1 Tbsp", new PantryItem("sugar"));

        PIZZA = new Recipe("Pizza");
        PIZZA.addIngredient("4 oz", new PantryItem("pepperoni"));
        PIZZA.addIngredient("8 oz", PIZZA_SAUCE);
        PIZZA.addIngredient("1", PIZZA_CRUST);
    }

}
