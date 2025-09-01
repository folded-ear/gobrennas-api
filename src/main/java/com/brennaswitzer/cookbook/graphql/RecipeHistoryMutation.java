package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.services.RecipeHistoryService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RecipeHistoryMutation {

    @Autowired
    private RecipeHistoryService service;

    @SchemaMapping(typeName = "RecipeHistoryMutation")
    public Long recipeId(DataFetchingEnvironment env) {
        return Long.valueOf((String) env.getExecutionStepInfo().getParent().getArguments().get("recipeId"));
    }

    @SchemaMapping(typeName = "RecipeHistoryMutation")
    public PlannedRecipeHistory setRating(@Argument Long id,
                                          @Argument Rating rating,
                                          @Argument Long ratingInt,
                                          DataFetchingEnvironment env) {
        if (rating == null) {
            rating = Rating.fromId(ratingInt);
        } else if (ratingInt != null && rating != Rating.fromId(ratingInt)) {
            throw new IllegalArgumentException(
                    "Only 'rating' or 'ratingInt' SHOULD be passed; if both are they MUST be equivalent.");
        }
        return service.setRating(recipeId(env), id, rating);
    }

    @SchemaMapping(typeName = "RecipeHistoryMutation")
    public PlannedRecipeHistory setNotes(@Argument Long id,
                                         @Argument String notes,
                                         DataFetchingEnvironment env) {
        return service.setNotes(recipeId(env), id, notes);
    }

}
