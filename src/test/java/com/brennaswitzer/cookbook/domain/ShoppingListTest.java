package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import org.junit.Test;

import java.util.Iterator;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ShoppingListTest {

    Consumer<Iterator<ShoppingList.Item>> checkPizzaItems = itr -> {
        // pizza
        // pepperoni is a raw ingredient
        // sauce
        assertEquals("fresh tomatoes (1 lbs)", itr.next().toString());
        assertEquals("tomato paste (1 (6 oz) can)", itr.next().toString());
        assertEquals("italian seasoning", itr.next().toString());
        assertEquals("salt (1.5 tsp)", itr.next().toString());
        // crust
        assertEquals("flour (2 c)", itr.next().toString());
        assertEquals("water (1 c)", itr.next().toString());
        assertEquals("yeast (1 packet)", itr.next().toString());
        assertEquals("sugar (1 Tbsp)", itr.next().toString());
    };

    @Test
    public void addPantryItems() {
        RecipeBox box = new RecipeBox();
        ShoppingList l = new ShoppingList();
        box.pizza.assemblePantryItemRefs().forEach(l::addPantryItem);
        assertEquals(8, l.getItems().size());
        Iterator<ShoppingList.Item> itr = l.getItems().iterator();
        checkPizzaItems.accept(itr);
        assertFalse(itr.hasNext());
    }

    @Test
    public void addAllPantryItems() {
        RecipeBox box = new RecipeBox();
        ShoppingList l = new ShoppingList();
        l.addAllPantryItems(box.pizza);
        assertEquals(8, l.getItems().size());
        Iterator<ShoppingList.Item> itr = l.getItems().iterator();
        checkPizzaItems.accept(itr);
        assertFalse(itr.hasNext());
    }

}