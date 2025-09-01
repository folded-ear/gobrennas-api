package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class LibraryMutation {

    @Autowired
    private RecipeHistoryMutation history;

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private Info2Recipe info2Recipe;

    @SchemaMapping(typeName = "LibraryMutation")
    @PreAuthorize("hasRole('USER')")
    public Recipe createRecipe(@Argument IngredientInfo info,
                               @Argument boolean cookThis,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
        info.setId(null);
        Recipe recipe = info2Recipe.convert(userPrincipal,
                                            info,
                                            cookThis);
        return recipeService.createNewRecipe(recipe, info);
    }

    @SchemaMapping(typeName = "LibraryMutation")
    @PreAuthorize("hasRole('USER')")
    public Recipe createRecipeFrom(@Argument Long sourceRecipeId,
                                   @Argument IngredientInfo info,
                                   @AuthenticationPrincipal UserPrincipal userPrincipal) {
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

    @SchemaMapping(typeName = "LibraryMutation")
    @PreAuthorize("hasRole('USER')")
    public Recipe updateRecipe(@Argument Long id,
                               @Argument IngredientInfo info,
                               @AuthenticationPrincipal UserPrincipal userPrincipal) {
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

    @SchemaMapping(typeName = "LibraryMutation")
    public Recipe setRecipePhoto(@Argument Long id,
                                 @Argument String filename,
                                 @Argument float[] focus) {
        return recipeService.setRecipePhoto(id, filename, focus);
    }

    @SchemaMapping(typeName = "LibraryMutation")
    public Deletion deleteRecipe(@Argument Long id) {
        return Deletion.of(recipeService.deleteRecipeById(id));
    }

    @SchemaMapping(typeName = "LibraryMutation")
    public RecipeHistoryMutation history(@Argument Long recipeId) {
        return history;
    }

    @SchemaMapping(typeName = "LibraryMutation")
    public PlanItem sendRecipeToPlan(@Argument Long id,
                                     @Argument Long planId,
                                     @Argument Double scale) {
        return recipeService.sendToPlan(id, planId, scale);
    }

}
