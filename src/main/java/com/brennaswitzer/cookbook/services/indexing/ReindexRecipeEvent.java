package com.brennaswitzer.cookbook.services.indexing;

public class ReindexRecipeEvent {

    private final Long recipeId;

    public ReindexRecipeEvent(Long recipeId) {
        this.recipeId = recipeId;
    }

    public Long getRecipeId() {
        return recipeId;
    }
    
}
