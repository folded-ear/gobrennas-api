package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.loaders.FavKey;
import com.brennaswitzer.cookbook.graphql.loaders.IsFavoriteBatchLoader;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.mapper.LabelMapper;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.util.ShareHelper;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Component
public class RecipeResolver implements GraphQLResolver<Recipe> {

    @Autowired
    private LabelMapper labelMapper;

    @Autowired
    private ShareHelper shareHelper;

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

    public CompletableFuture<Boolean> favorite(Recipe recipe,
                                               DataFetchingEnvironment env) {
        return env.<FavKey, Boolean>getDataLoader(IsFavoriteBatchLoader.class.getName())
                .load(new FavKey(PrincipalUtil.from(env).getId(),
                                 FavoriteType.RECIPE,
                                 recipe.getId()));
    }

    public Photo photo(Recipe recipe) {
        return recipe.hasPhoto()
                ? recipe.getPhoto()
                : null;
    }

    public List<IngredientRef> ingredients(Recipe recipe, Set<Long> ingredientIds) {
        if (ingredientIds == null || ingredientIds.isEmpty()) {
            return recipe.getIngredients();
        }
        return recipe.getIngredients()
                .stream()
                .filter(r -> r.hasIngredient()
                             && ingredientIds.contains(r.getIngredient().getId()))
                .toList();
    }

    public Collection<Recipe> subrecipes(Recipe recipe) {
        Collection<Recipe> result = new LinkedHashSet<>();
        Queue<IngredientRef> queue = new LinkedList<>(recipe.getIngredients());
        while (!queue.isEmpty()) {
            IngredientRef ir = queue.remove();
            if (!ir.hasIngredient()) continue;
            var ing = Hibernate.unproxy(ir.getIngredient(), Ingredient.class);
            if (ing instanceof Recipe r && result.add(r)) {
                queue.addAll(r.getIngredients());
            }
        }
        return result;
    }

    public int plannedCount(Recipe recipe, PlanItemStatus status) {
        if (status == null) {
            // don't materialize the collection if we don't need to.
            return recipe.getPlanHistory().size();
        }
        return (int) recipe.getPlanHistory()
                .stream()
                .filter(ofStatus(status))
                .count();
    }

    public List<PlannedRecipeHistory> plannedHistory(Recipe recipe, PlanItemStatus status, int last) {
        return recipe.getPlanHistory()
                .stream()
                .sorted(PlannedRecipeHistory.BY_RECENT)
                .filter(ofStatus(status))
                .limit(last)
                .toList();
    }

    public ShareInfo share(Recipe recipe) {
        return shareHelper.getInfo(Recipe.class, recipe);
    }

    private Predicate<PlannedRecipeHistory> ofStatus(PlanItemStatus status) {
        return status == null
                ? it -> true
                : it -> status == it.getStatus();
    }

}
