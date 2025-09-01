package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.services.RecipeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RecipeHistoryMutationController {

    record RecipeHistoryMutation(Long recipeId) {}

    @Autowired
    private RecipeHistoryService service;

    @SchemaMapping
    RecipeHistoryMutation history(LibraryMutationController.LibraryMutation libMut,
                                  @Argument Long recipeId) {
        return new RecipeHistoryMutation(recipeId);
    }

    @SchemaMapping
    Long recipeId(RecipeHistoryMutation histMut) {
        return histMut.recipeId();
    }

    @SchemaMapping
    PlannedRecipeHistory setRating(RecipeHistoryMutation histMut,
                                   @Argument Long id,
                                   @Argument Rating rating,
                                   @Argument Long ratingInt) {
        if (rating == null) {
            rating = Rating.fromId(ratingInt);
        } else if (ratingInt != null && rating != Rating.fromId(ratingInt)) {
            throw new IllegalArgumentException(
                    "Only 'rating' or 'ratingInt' SHOULD be passed; if both are they MUST be equivalent.");
        }
        return service.setRating(histMut.recipeId(), id, rating);
    }

    @SchemaMapping
    PlannedRecipeHistory setNotes(RecipeHistoryMutation histMut,
                                  @Argument Long id,
                                  @Argument String notes) {
        return service.setNotes(histMut.recipeId(), id, notes);
    }

}
