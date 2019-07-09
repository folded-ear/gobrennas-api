package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class RecipeTest {

    @Test
    public void displayTitle() throws Exception {
        Field tf = Recipe.class.getDeclaredField("displayTitle");
        tf.setAccessible(true);
        try {
            Recipe r = new Recipe("fred");
            assertEquals("fred", r.getName());
            assertEquals("fred", r.getDisplayTitle());
            assertNull(tf.get(r));

            r.setDisplayTitle("Frederick");
            assertEquals("fred", r.getName());
            assertEquals("Frederick", r.getDisplayTitle());
            assertEquals("Frederick", tf.get(r));

            r.setDisplayTitle("fred");
            assertEquals("fred", r.getName());
            assertEquals("fred", r.getDisplayTitle());
            assertNull(tf.get(r));

            r.setDisplayTitle("Frederick");
            r.setName("Frederick");
            assertEquals("Frederick", r.getName());
            assertEquals("Frederick", r.getDisplayTitle());
            assertNull(tf.get(r));
        } finally {
            tf.setAccessible(false);
        }
    }

    @Test
    public void emptySchmankies() {
        Recipe r = new Recipe();
        assertTrue(r.getPurchasableSchmankies().isEmpty());
    }

    @Test
    public void simpleSchmankies() {
        RecipeBox box = new RecipeBox();
        List<IngredientRef> pss = new ArrayList<>(box.friedChicken.getPurchasableSchmankies());
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
        RecipeBox box = new RecipeBox();
        List<IngredientRef> pss = new ArrayList<>(box.pizza.getPurchasableSchmankies());
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