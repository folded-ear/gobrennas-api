package com.brennaswitzer.cookbook.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class RecipeTest {

    @Test
    public void emptySchmankies() {
        Recipe r = new Recipe();
        assertTrue(r.getPurchasableSchmankies().isEmpty());
    }

    @Test
    public void simpleSchmankies() {
        Recipe r = new Recipe("Fried Chicken");

        PantryItem egg = new PantryItem("egg");
        r.addIngredient("2", egg, "shelled");
        PantryItem chicken = new PantryItem("chicken");
        r.addIngredient(chicken, "deboned");

        List<IngredientRef> pss = new ArrayList<>(r.getPurchasableSchmankies());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(2, pss.size());
        Iterator<IngredientRef> itr = pss.iterator();
        IngredientRef ref = itr.next();
        assertEquals(chicken, ref.getIngredient());
        assertEquals("deboned", ref.getPreparation());
        ref = itr.next();
        assertEquals(egg, ref.getIngredient());
        assertEquals("2", ref.getQuantity());
        assertEquals("shelled", ref.getPreparation());
        assertFalse(itr.hasNext());
    }

    @Test
    public void recursiveSchmankies() {
        Recipe sauce = new Recipe("Pizza Sauce");
        sauce.addIngredient("1 lbs", new PantryItem("fresh tomatoes"), "seeded and crushed");
        sauce.addIngredient("1 (6 oz) can", new PantryItem("tomato paste"));
        sauce.addIngredient(new PantryItem("italian seasoning"));

        Recipe crust = new Recipe("Pizza Crust");
        crust.addIngredient("2 c", new PantryItem("flour"));
        crust.addIngredient("1 c", new PantryItem("water"));
        crust.addIngredient("1 packet", new PantryItem("yeast"));
        crust.addIngredient("1 Tbsp", new PantryItem("sugar"));

        Recipe pizza = new Recipe("Pizza");
        pizza.addIngredient("4 oz", new PantryItem("pepperoni"));
        pizza.addIngredient("8 oz", sauce);
        pizza.addIngredient("1", crust);

        List<IngredientRef> pss = new ArrayList<>(pizza.getPurchasableSchmankies());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(8, pss.size());
        Iterator<IngredientRef> itr = pss.iterator();

        IngredientRef ref;

        assertEquals("flour", itr.next().getIngredient().getName());
        ref = itr.next();
        assertEquals("fresh tomatoes", ref.getIngredient().getName());
            assertEquals("1 lbs", ref.getQuantity());
            assertEquals("seeded and crushed", ref.getPreparation());
        assertEquals("italian seasoning", itr.next().getIngredient().getName());
        ref = itr.next();
        assertEquals("pepperoni", ref.getIngredient().getName());
            assertEquals("4 oz", ref.getQuantity());
            assertNull(ref.getPreparation());
        assertEquals("sugar", itr.next().getIngredient().getName());
        assertEquals("tomato paste", itr.next().getIngredient().getName());
        assertEquals("water", itr.next().getIngredient().getName());
        assertEquals("yeast", itr.next().getIngredient().getName());
        assertFalse(itr.hasNext());
    }

}