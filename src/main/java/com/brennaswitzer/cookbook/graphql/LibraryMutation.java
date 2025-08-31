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
import org.springframework.stereotype.Component;

@Component
public class LibraryMutation {

    @Autowired
    private RecipeHistoryMutation history;

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private Info2Recipe info2Recipe;

    public Recipe createRecipe(IngredientInfo info,
                               boolean cookThis,
                               DataFetchingEnvironment env) {
        info.setId(null);
        Recipe recipe = info2Recipe.convert(PrincipalUtil.from(env),
                                            info,
                                            cookThis);
        return recipeService.createNewRecipe(recipe, info);
    }

    public Recipe createRecipeFrom(Long sourceRecipeId,
                                   IngredientInfo info,
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

    public Recipe updateRecipe(Long id,
                               IngredientInfo info,
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

    public Recipe setRecipePhoto(Long id,
                                 String filename,
                                 float[] focus) {
        return recipeService.setRecipePhoto(id, filename, focus);
    }

    public Deletion deleteRecipe(Long id) {
        return Deletion.of(recipeService.deleteRecipeById(id));
    }

    public RecipeHistoryMutation history(Long recipeId) {
        return history;
    }

    public PlanItem sendRecipeToPlan(Long id, Long planId, Double scale) {
        return recipeService.sendToPlan(id, planId, scale);
    }

}
