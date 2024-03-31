package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.mapper.LabelMapper;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class RecipeResolver implements GraphQLResolver<Recipe> {

    @Autowired
    private FetchFavorites fetchFavorites;

    @Autowired
    private LabelMapper labelMapper;

    public Integer totalTime(Recipe recipe, ChronoUnit unit) {
        Integer millis = recipe.getTotalTime();
        if (millis == null || unit == ChronoUnit.MILLIS) {
            return millis;
        }
        return millis / (int) unit.getDuration().toMillis();
    }

    public List<String> labels(Recipe recipe) {
        return recipe.getLabels()
                .stream()
                .map(labelMapper::labelToString)
                .collect(toList());
    }

    public boolean favorite(Recipe recipe) {
        return fetchFavorites.byObject(
                        FavoriteType.RECIPE.getKey(),
                        recipe.getId())
                .isPresent();
    }

    public Photo photo(Recipe recipe) {
        return recipe.hasPhoto()
                ? recipe.getPhoto()
                : null;
    }

    public List<Recipe> subrecipes(Recipe recipe) {
        List<Recipe> result = new ArrayList<>();
        Queue<IngredientRef> queue = new LinkedList<>(recipe.getIngredients());
        while (!queue.isEmpty()) {
            IngredientRef ir = queue.remove();
            if (!ir.hasIngredient()) continue;
            Ingredient i = ir.getIngredient();
            if (i instanceof Recipe) {
                result.add((Recipe) i);
                queue.addAll(((AggregateIngredient) i).getIngredients());
            }
        }
        return result;
    }

}
