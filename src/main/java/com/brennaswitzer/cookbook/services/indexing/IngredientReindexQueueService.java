package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;

public interface IngredientReindexQueueService {

    IndexStats getIndexStats();

    void enqueueIngredient(Ingredient ingredient);

    void enqueueRecipesWithIngredient(Ingredient ingredient);

    void enqueueIngredientsWithLabel(Label label);
}
