package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

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
