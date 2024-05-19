package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.google.common.annotations.VisibleForTesting;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class RecipeIsFavoriteBatchLoader implements BatchLoader<Recipe, Boolean> {

    @Autowired
    private FavoriteRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Override
    public CompletionStage<List<Boolean>> load(List<Recipe> recipes) {
        // Graphql will complete the future on a thread from its own pool,
        // outside Spring's context, so retrieve the owner synchronously.
        User owner = principalAccess.getUser();
        return CompletableFuture.supplyAsync(() -> loadInternal(owner, recipes));
    }

    @VisibleForTesting
    List<Boolean> loadInternal(User owner, List<Recipe> recipes) {
        List<Long> ownedRecipeIds = recipes.stream()
                .filter(owner::isOwnerOf)
                .map(Recipe::getId)
                .toList();
        Set<Long> favoriteIds;
        if (ownedRecipeIds.isEmpty()) {
            favoriteIds = Collections.emptySet();
        } else {
            favoriteIds = new HashSet<>();
            for (var f : repo.findByOwnerAndObjectTypeAndObjectIdIn(
                    owner,
                    FavoriteType.RECIPE.getKey(),
                    ownedRecipeIds)) {
                favoriteIds.add(f.getObjectId());
            }
        }
        return recipes.stream()
                .map(Recipe::getId)
                .map(favoriteIds::contains)
                .toList();
    }

}
