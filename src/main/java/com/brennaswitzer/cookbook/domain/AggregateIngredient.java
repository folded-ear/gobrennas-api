package com.brennaswitzer.cookbook.domain;

import java.util.Collection;

public interface AggregateIngredient {

    Long getId();

    String getName();

    Collection<IngredientRef> getIngredients();

    default IngredientRef addIngredient(Ingredient ingredient) {
        return addIngredient(null, ingredient, null);
    }

    default IngredientRef addIngredient(Quantity quantity, Ingredient ingredient) {
        return addIngredient(quantity, ingredient, null);
    }

    default IngredientRef addIngredient(Ingredient ingredient, String preparation) {
        return addIngredient(Quantity.ONE, ingredient, preparation);
    }

    IngredientRef addIngredient(Quantity quantity, Ingredient ingredient, String preparation);

    /**
     * I return the PantryItem IngredientRefs for this Ingredient, including
     * those referenced recursively through nested `AggregateIngredient`s.
     */
    Collection<IngredientRef> assemblePantryItemRefs();

    /**
     * I return all the "raw" IngredientRefs for this Ingredient, including
     * those referenced recursively through nested `AggregateIngredient`s.
     */
    Collection<IngredientRef> assembleRawIngredientRefs();

}
