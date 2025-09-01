package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.support.Info2Recipe;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.RecipeService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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
    public Recipe createRecipe(@Argument IngredientInfo info,
                               @Argument boolean cookThis,
                               DataFetchingEnvironment env) {
        info.setId(null);
        Recipe recipe = info2Recipe.convert(PrincipalUtil.from(env),
                                            info,
                                            cookThis);
        return recipeService.createNewRecipe(recipe, info);
    }

    @SchemaMapping(typeName = "LibraryMutation")
    public Recipe createRecipeFrom(@Argument Long sourceRecipeId,
                                   @Argument IngredientInfo info,
                                   DataFetchingEnvironment env) {
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
        Recipe recipe = info2Recipe.convert(PrincipalUtil.from(env),
                                            info);
        return recipeService.createNewRecipeFrom(sourceRecipeId, recipe, info);
    }

    @SchemaMapping(typeName = "LibraryMutation")
    public Recipe updateRecipe(@Argument Long id,
                               @Argument IngredientInfo info,
                               DataFetchingEnvironment env) {
        if (id != null) {
            if (info.getId() != null && !id.equals(info.getId())) {
                throw new IllegalArgumentException(String.format(
                        "Cannot update recipe '%s' with info from '%s'",
                        id,
                        info.getId()));
            }
            info.setId(id);
        }
        Recipe recipe = info2Recipe.convert(PrincipalUtil.from(env),
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
