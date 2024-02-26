package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Upload;
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

    public boolean deleteRecipe(Long id) {
        recipeService.deleteRecipeById(id);
        return true;
    }

}
