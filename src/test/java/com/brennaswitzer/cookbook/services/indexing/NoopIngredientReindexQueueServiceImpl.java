package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Label;
import org.springframework.stereotype.Service;

@Service
public class NoopIngredientReindexQueueServiceImpl implements IngredientReindexQueueService {

    @Override
    public IndexStats getIndexStats() {
        return IndexStats.builder().build();
    }

    @Override
    public void enqueueIngredient(Ingredient ingredient) {
    }

    @Override
    public void enqueueRecipesWithIngredient(Ingredient ingredient) {
    }

    @Override
    public void enqueueIngredientsWithLabel(Label label) {
    }
}
