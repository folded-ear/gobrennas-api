package com.brennaswitzer.cookbook.domain;

import java.util.Collection;

public interface AggregateIngredient {

    Long getId();

    String getName();

    Collection<IngredientRef> getIngredients();

    void addIngredient(Ingredient ingredient);
    void addIngredient(Float quantity, Ingredient ingredient);
    void addIngredient(Float quantity, String units, Ingredient ingredient);
    void addIngredient(Ingredient ingredient, String preparation);
    void addIngredient(Float quantity, Ingredient ingredient, String preparation);
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
