package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;

public interface RecipeReindexQueueService {

    IndexStats getIndexStats();

    void enqueueRecipe(Recipe recipe);

    void enqueueRecipesWithIngredient(Ingredient ingredient);

    void enqueueRecipesWithLabel(Label label);
}
