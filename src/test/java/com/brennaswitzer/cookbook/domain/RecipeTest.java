package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class RecipeTest {

    @Test
    public void emptyPantryItems() {
        Recipe r = new Recipe();
        assertTrue(r.assemblePantryItemRefs().isEmpty());
    }

    @Test
    public void directPantryItems() {
        RecipeBox box = new RecipeBox();
        List<IngredientRef> pss = new ArrayList<>(box.friedChicken.assemblePantryItemRefs());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(3, pss.size());
        Iterator<IngredientRef> itr = pss.iterator();
        IngredientRef ref = itr.next();
        assertEquals("chicken", ref.getIngredient().getName());
        assertEquals("deboned", ref.getPreparation());
        itr.next(); // skip over the chicken thighs
        ref = itr.next();
        assertEquals("egg", ref.getIngredient().getName());
        assertEquals(Quantity.count(2), ref.getQuantity());
        assertEquals("shelled", ref.getPreparation());
        assertFalse(itr.hasNext());
    }

    @Test
    public void recursivePantryItems() {
        RecipeBox box = new RecipeBox();
        List<IngredientRef> pss = new ArrayList<>(box.pizza.assemblePantryItemRefs());
        pss.sort(IngredientRef.BY_INGREDIENT_NAME);
        assertEquals(10, pss.size());
        Iterator<IngredientRef> itr = pss.iterator();

        IngredientRef ref;

        assertEquals("flour", itr.next().getIngredient().getName());
        ref = itr.next();
        assertEquals("fresh tomatoes", ref.getIngredient().getName());
            assertEquals(new Quantity(1, box.lbs), ref.getQuantity());
            assertEquals("seeded and crushed", ref.getPreparation());
        assertEquals("italian seasoning", itr.next().getIngredient().getName());
        // pepperoni's a raw ingredient
        assertEquals("oil", itr.next().getIngredient().getName());
        assertEquals("salt", itr.next().getIngredient().getName());
        assertEquals("salt", itr.next().getIngredient().getName());
        assertEquals("sugar", itr.next().getIngredient().getName());
        assertEquals("tomato paste", itr.next().getIngredient().getName());
        assertEquals("water", itr.next().getIngredient().getName());
        assertEquals("yeast", itr.next().getIngredient().getName());
        assertFalse(itr.hasNext());
    }

}
