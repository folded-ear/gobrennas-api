package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;

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
