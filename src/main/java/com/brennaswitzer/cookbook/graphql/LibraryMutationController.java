package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class LibraryMutationController {

    record LibraryMutation() {}

    @Autowired
    private RecipeHistoryMutationController history;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private Info2Recipe info2Recipe;

    @MutationMapping
    LibraryMutation library() {
        return new LibraryMutation();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Recipe createRecipe(LibraryMutation libMut,
                        @Argument IngredientInfo info,
                        @Argument boolean cookThis,
                        @CurrentUser UserPrincipal userPrincipal) {
        info.setId(null);
        Recipe recipe = info2Recipe.convert(userPrincipal,
                                            info,
                                            cookThis);
        return recipeService.createNewRecipe(recipe, info);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Recipe createRecipeFrom(LibraryMutation libMut,
                            @Argument Long sourceRecipeId,
                            @Argument IngredientInfo info,
                            @CurrentUser UserPrincipal userPrincipal) {
        if (info.getId() != null) {
            if (sourceRecipeId != null && !info.getId().equals(sourceRecipeId)) {
                throw new IllegalArgumentException(String.format(
                        "Cannot create from '%s' with info from '%s'",
                        sourceRecipeId,
                        info.getId()));
            }
            sourceRecipeId = info.getId();
            info.setId(null);
        }
        Recipe recipe = info2Recipe.convert(userPrincipal,
                                            info);
        return recipeService.createNewRecipeFrom(sourceRecipeId, recipe, info);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Recipe updateRecipe(LibraryMutation libMut,
                        @Argument Long id,
                        @Argument IngredientInfo info,
                        @CurrentUser UserPrincipal userPrincipal) {
        if (id != null) {
            if (info.getId() != null && !id.equals(info.getId())) {
                throw new IllegalArgumentException(String.format(
                        "Cannot update recipe '%s' with info from '%s'",
                        id,
                        info.getId()));
            }
            info.setId(id);
        }
        Recipe recipe = info2Recipe.convert(userPrincipal,
                                            info);
        return recipeService.updateRecipe(recipe, info);
    }

    @SchemaMapping
    Recipe setRecipePhoto(LibraryMutation libMut,
                          @Argument Long id,
                          @Argument String filename,
                          @Argument List<Float> focus) {
        return recipeService.setRecipePhoto(id, filename, focus);
    }

    @SchemaMapping
    Deletion deleteRecipe(LibraryMutation libMut,
                          @Argument Long id) {
        return Deletion.of(recipeService.deleteRecipeById(id));
    }

    @SchemaMapping
    PlanItem sendRecipeToPlan(LibraryMutation libMut,
                              @Argument Long id,
                              @Argument Long planId,
                              @Argument Double scale) {
        return recipeService.sendToPlan(id, planId, scale);
    }

}
