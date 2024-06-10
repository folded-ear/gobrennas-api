package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.services.RecipeHistoryService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class RecipeHistoryMutation {

    @Autowired
    private RecipeHistoryService service;

    public Long recipeId(DataFetchingEnvironment env) {
        return Long.valueOf((String) env.getExecutionStepInfo().getParent().getArguments().get("recipeId"));
    }

    public PlannedRecipeHistory setRating(Long id,
                                          Rating rating,
                                          Long ratingInt,
                                          DataFetchingEnvironment env) {
        if (rating == null) {
            rating = Rating.fromId(ratingInt);
        } else if (ratingInt != null && rating != Rating.fromId(ratingInt)) {
            throw new IllegalArgumentException(
                    "Only 'rating' or 'ratingInt' SHOULD be passed; if both are they MUST be equivalent.");
        }
        return service.setRating(recipeId(env), id, rating);
    }

    public PlannedRecipeHistory setNotes(Long id,
                                         String notes,
                                         DataFetchingEnvironment env) {
        return service.setNotes(recipeId(env), id, notes);
    }

}
