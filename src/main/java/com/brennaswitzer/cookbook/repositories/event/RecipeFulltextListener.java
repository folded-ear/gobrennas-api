package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecipeFulltextListener {

    @Autowired
    private RecipeReindexQueueService service;

    @PostPersist
    @PostUpdate
    public void onEvent(Recipe recipe) {
        service.enqueueRecipe(recipe);
    }

}
