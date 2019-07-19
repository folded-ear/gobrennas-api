package com.brennaswitzer.cookbook.domain;

import java.util.Collection;

public interface AggregateIngredient {

    Long getId();

    String getName();

    Collection<IngredientRef> getIngredients();

    default void addIngredient(Ingredient ingredient) {
        addIngredient(null, ingredient, null);
    }

    default void addIngredient(Float quantity, Ingredient ingredient) {
        addIngredient(quantity, ingredient, null);
    }

    default void addIngredient(Float quantity, String units, Ingredient ingredient) {
        addIngredient(quantity, units, ingredient, null);
    }

    default void addIngredient(Ingredient ingredient, String preparation) {
        addIngredient(null, ingredient, preparation);
    }

    default void addIngredient(Float quantity, Ingredient ingredient, String preparation) {
        addIngredient(quantity, null, ingredient, preparation);
    }

    void addIngredient(Float quantity, String units, Ingredient ingredient, String preparation);

    /**
     * I return the PantryItem IngredientRefs for this Ingredient, including
     * those referenced recursively through nested `AggregateIngredient`s.
     */
    Collection<IngredientRef<PantryItem>> assemblePantryItemRefs();

    /**
     * I return all the "raw" IngredientRefs for this Ingredient, including
     * those referenced recursively through nested `AggregateIngredient`s.
     */
    Collection<IngredientRef> assembleRawIngredientRefs();

}
