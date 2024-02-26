package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecipeLabelFulltextListener {

    @Autowired
    private RecipeReindexQueueService service;

    @PostUpdate
    @PreRemove
    public void onEvent(Label label) {
        service.enqueueRecipesWithLabel(label);
    }

}
