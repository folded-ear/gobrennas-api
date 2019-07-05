package com.brennaswitzer.cookbook.domain;

import java.util.Collection;

public interface AggregateIngredient {

    String getName();

    Collection<IngredientRef> getIngredients();

    void addIngredient(Ingredient ingredient);
    void addIngredient(String quantity, Ingredient ingredient);
    void addIngredient(Ingredient ingredient, String preparation);
    void addIngredient(String quantity, Ingredient ingredient, String preparation);

    /**
     * I return the stuff for this Ingredient that you'd purchase at a store.
     * I'm deliberately named in a gratuitously nonsensical manner. For now. :)
     */
    Collection<IngredientRef> getPurchasableSchmankies();

}
