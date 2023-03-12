package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class RecipeResolver implements GraphQLResolver<Recipe> {

    @Autowired
    private FetchFavorites fetchFavorites;

    public boolean isFavorite(Recipe recipe) {
        return fetchFavorites.byObject(
                        FavoriteType.RECIPE.getKey(),
                        recipe.getId())
                .isPresent();
    }

}
