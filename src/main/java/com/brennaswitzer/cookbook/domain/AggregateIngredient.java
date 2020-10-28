package com.brennaswitzer.cookbook.domain;

import java.util.Collection;

public interface AggregateIngredient {

    Long getId();

    String getName();

    Collection<IngredientRef> getIngredients();

    default void addIngredient(Ingredient ingredient) {
        addIngredient(Quantity.ONE, ingredient, null);
    }

    default void addIngredient(Quantity quantity, Ingredient ingredient) {
        addIngredient(quantity, ingredient, null);
    }

    default void addIngredient(Ingredient ingredient, String preparation) {
        addIngredient(Quantity.ONE, ingredient, preparation);
    }

    void addIngredient(Quantity quantity, Ingredient ingredient, String preparation);

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
