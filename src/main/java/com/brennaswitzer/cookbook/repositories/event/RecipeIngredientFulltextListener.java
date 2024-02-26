package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecipeIngredientFulltextListener {

    @Autowired
    private RecipeReindexQueueService service;

    @PostUpdate
    @PreRemove
    public void onEvent(Ingredient ingredient) {
        service.enqueueRecipesWithIngredient(ingredient);
    }

}
