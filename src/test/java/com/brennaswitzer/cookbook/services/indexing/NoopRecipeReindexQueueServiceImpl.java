package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import org.springframework.stereotype.Service;

@Service
public class NoopRecipeReindexQueueServiceImpl implements RecipeReindexQueueService {
    @Override
    public void enqueueRecipe(Recipe recipe) {
    }

    @Override
    public void enqueueRecipesWithIngredient(Ingredient ingredient) {
    }

    @Override
    public void enqueueRecipesWithLabel(Label label) {
    }
}
