package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.services.indexing.IngredientReindexQueueService;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IngredientFulltextListener {

    @Autowired
    private IngredientReindexQueueService service;

    @PostPersist
    @PostUpdate
    public void onEvent(Ingredient ingredient) {
        service.enqueueIngredient(ingredient);
    }

}
