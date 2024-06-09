package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.LabelService;
import com.brennaswitzer.cookbook.services.RecipeService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LibraryMutation {

    @Autowired
    private RecipeHistoryMutation history;

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private EntityManager entityManager;

    public Recipe createRecipe(IngredientInfo info, Part photo, boolean cookThis) {
        Recipe recipe = info.asRecipe(entityManager);
        if (cookThis) {
            recipe.getIngredients().forEach(itemService::autoRecognize);
        }
        recipe = recipeService.createNewRecipe(recipe, Upload.of(photo));
        labelService.updateLabels(recipe, info.getLabels());
        return recipe;
    }

    public Recipe updateRecipe(Long id, IngredientInfo info, Part photo) {
        info.setId(id);
        Recipe recipe = info.asRecipe(entityManager);
        recipe = recipeService.updateRecipe(recipe, Upload.of(photo));
        labelService.updateLabels(recipe, info.getLabels());
        return recipe;
    }

    public Recipe setRecipePhoto(Long id, Part photo) {
        return recipeService.setRecipePhoto(id, Upload.of(photo));
    }

    public Deletion deleteRecipe(Long id) {
        return Deletion.of(recipeService.deleteRecipeById(id));
    }

    public RecipeHistoryMutation history(Long recipeId) {
        return history;
    }

}
