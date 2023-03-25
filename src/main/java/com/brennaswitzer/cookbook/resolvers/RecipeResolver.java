package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.mapper.LabelMapper;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class RecipeResolver implements GraphQLResolver<Recipe> {

    @Autowired
    private FetchFavorites fetchFavorites;

    @Autowired
    private LabelMapper labelMapper;

    public Integer getTotalTime(Recipe recipe, ChronoUnit unit) {
        Integer millis = recipe.getTotalTime();
        if (millis == null || unit == ChronoUnit.MILLIS) {
            return millis;
        }
        return millis / (int) unit.getDuration().toMillis();
    }

    public List<String> getLabels(Recipe recipe) {
        return recipe.getLabels()
            .stream()
            .map(labelMapper::labelToString)
            .collect(toList());
    }

    public boolean isFavorite(Recipe recipe) {
        return fetchFavorites.byObject(
                        FavoriteType.RECIPE.getKey(),
                        recipe.getId())
                .isPresent();
    }

    public Photo getPhoto(Recipe recipe) {
        return recipe.hasPhoto()
                ? recipe.getPhoto()
                : null;
    }

}
