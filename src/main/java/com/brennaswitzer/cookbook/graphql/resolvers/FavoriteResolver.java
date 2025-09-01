package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FavoriteResolver {

    @Autowired
    private RecipeService recipeService;

    @SchemaMapping
    public String name(Favorite favorite) {
        if (FavoriteType.RECIPE.matches(favorite.getObjectType())) {
            return recipeService.findRecipeById(favorite.getObjectId())
                    .map(Recipe::getName)
                    .orElseGet(() -> getDefaultName(favorite));
        } else {
            return getDefaultName(favorite);
        }
    }

    private static String getDefaultName(Favorite favorite) {
        return String.format("%s#%d",
                             favorite.getObjectType(),
                             favorite.getObjectId());
    }

}
