package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.indexing.IngredientReindexQueueService;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IngredientLabelFulltextListener {

    @Autowired
    private IngredientReindexQueueService service;

    @PostUpdate
    @PreRemove
    public void onEvent(Label label) {
        service.enqueueIngredientsWithLabel(label);
    }

}
