package com.brennaswitzer.cookbook.repositories.event;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.services.RecipeFulltextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;

@Component
public class RecipeLabelFulltextListener {

    @Autowired
    private RecipeFulltextService service;

    @PostUpdate
    @PreRemove
    public void onEvent(Label label) {
        service.enqueueRecipesWithLabel(label);
    }

}
