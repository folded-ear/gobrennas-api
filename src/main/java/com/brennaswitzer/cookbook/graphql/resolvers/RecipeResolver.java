package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.graphql.loaders.FavKey;
import com.brennaswitzer.cookbook.graphql.loaders.IsFavoriteBatchLoader;
import com.brennaswitzer.cookbook.graphql.model.Section;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.util.ShareHelper;
import graphql.schema.DataFetchingEnvironment;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Controller
public class RecipeResolver {

    @Autowired
    private ShareHelper shareHelper;

    @SchemaMapping
    public Integer totalTime(Recipe recipe,
                             @Argument ChronoUnit unit) {
        Integer millis = recipe.getTotalTime();
        if (millis == null || unit == ChronoUnit.MILLIS) {
            return millis;
        }
        return millis / (int) unit.getDuration().toMillis();
    }

    @SchemaMapping
    public List<String> labels(Recipe recipe) {
        return recipe.getLabels()
                .stream()
                .map(Label::getName)
                .toList();
    }

    @SchemaMapping
    public CompletableFuture<Boolean> favorite(Recipe recipe,
                                               DataFetchingEnvironment env) {
        return env.<FavKey, Boolean>getDataLoader(IsFavoriteBatchLoader.class.getName())
                .load(new FavKey(PrincipalUtil.from(env).getId(),
                                 FavoriteType.RECIPE,
                                 recipe.getId()));
    }

    @SchemaMapping
    public Photo photo(Recipe recipe) {
        return recipe.hasPhoto()
                ? recipe.getPhoto()
                : null;
    }

    @SchemaMapping
    public List<IngredientRef> ingredients(Recipe recipe,
                                           @Argument("ingredients") Set<Long> ingredientIds) {
        Stream<IngredientRef> allIngredients = recipe.getIngredients()
                .stream()
                .filter(not(Section::isSection));
        if (ingredientIds != null && !ingredientIds.isEmpty()) {
            allIngredients = allIngredients
                    .filter(r -> r.hasIngredient()
                                 && ingredientIds.contains(r.getIngredient().getId()));
        }
        return allIngredients
                .toList();
    }

    @SchemaMapping
    public Collection<Recipe> subrecipes(Recipe recipe) {
        Collection<Recipe> result = new LinkedHashSet<>();
        Queue<IngredientRef> queue = recipe.getIngredients()
                .stream()
                .filter(not(IngredientRef::isSection))
                .collect(Collectors.toCollection(LinkedList::new));
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

    @SchemaMapping
    public int plannedCount(Recipe recipe,
                            @Argument PlanItemStatus status) {
        if (status == null) {
            // don't materialize the collection if we don't need to.
            return recipe.getPlanHistory().size();
        }
        return (int) recipe.getPlanHistory()
                .stream()
                .filter(ofStatus(status))
                .count();
    }

    @SchemaMapping
    public List<PlannedRecipeHistory> plannedHistory(Recipe recipe,
                                                     @Argument PlanItemStatus status,
                                                     @Argument int last) {
        return recipe.getPlanHistory()
                .stream()
                .sorted(PlannedRecipeHistory.BY_RECENT)
                .filter(ofStatus(status))
                .limit(last)
                .toList();
    }

    @SchemaMapping
    public ShareInfo share(Recipe recipe) {
        return shareHelper.getInfo(Recipe.class, recipe);
    }

    @SchemaMapping
    public List<Section> sections(Recipe recipe) {
        return recipe.getIngredients()
                .stream()
                .filter(Section::isSection)
                .map(Section::from)
                .toList();
    }

    private Predicate<PlannedRecipeHistory> ofStatus(PlanItemStatus status) {
        return status == null
                ? it -> true
                : it -> status == it.getStatus();
    }

}
