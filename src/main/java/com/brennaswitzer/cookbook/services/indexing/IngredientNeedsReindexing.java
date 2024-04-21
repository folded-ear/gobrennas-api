package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.domain.Ingredient;

public record IngredientNeedsReindexing(Ingredient ingredient) {

    public Long id() {
        return ingredient.getId();
    }

}
