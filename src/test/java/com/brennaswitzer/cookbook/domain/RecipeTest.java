package com.brennaswitzer.cookbook.domain;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.brennaswitzer.cookbook.util.RecipeBox.FRIED_CHICKEN;
import static com.brennaswitzer.cookbook.util.RecipeBox.PIZZA;
import static org.junit.Assert.*;

public class RecipeTest {

    @Test
    public void emptySchmankies() {
        Recipe r = new Recipe();
        assertTrue(r.getPurchasableSchmankies().isEmpty());
    }

    @Test
    public void simpleSchmankies() {
        List<IngredientRef> pss = new ArrayList<>(FRIED_CHICKEN.getPurchasableSchmankies());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(2, pss.size());
        Iterator<IngredientRef> itr = pss.iterator();
        IngredientRef ref = itr.next();
        assertEquals("chicken", ref.getIngredient().getName());
        assertEquals("deboned", ref.getPreparation());
        ref = itr.next();
        assertEquals("egg", ref.getIngredient().getName());
        assertEquals("2", ref.getQuantity());
        assertEquals("shelled", ref.getPreparation());
        assertFalse(itr.hasNext());
    }

    @Test
    public void recursiveSchmankies() {
        List<IngredientRef> pss = new ArrayList<>(PIZZA.getPurchasableSchmankies());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(10, pss.size());
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
        assertEquals("salt", itr.next().getIngredient().getName());
        assertEquals("salt", itr.next().getIngredient().getName());
        assertEquals("sugar", itr.next().getIngredient().getName());
        assertEquals("tomato paste", itr.next().getIngredient().getName());
        assertEquals("water", itr.next().getIngredient().getName());
        assertEquals("yeast", itr.next().getIngredient().getName());
        assertFalse(itr.hasNext());
    }

}