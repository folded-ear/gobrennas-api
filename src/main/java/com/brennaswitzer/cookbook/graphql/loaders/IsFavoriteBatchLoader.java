package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.google.common.annotations.VisibleForTesting;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;

@Component
public class IsFavoriteBatchLoader implements BatchLoader<FavKey, Boolean> {

    @Autowired
    private FavoriteRepository repo;

    private record OwnerType(long ownerId, FavoriteType favType) {

        public static OwnerType of(FavKey key) {
            return new OwnerType(key.ownerId(), key.favType());
        }

    }

    @Override
    public CompletionStage<List<Boolean>> load(List<FavKey> keys) {
        return CompletableFuture.supplyAsync(() -> loadInternal(keys));
    }

    @VisibleForTesting
    List<Boolean> loadInternal(List<FavKey> keys) {
        var favsByOwnerType = keys.stream()
                .collect(groupingBy(OwnerType::of,
                                    mapping(FavKey::objectId,
                                            Collectors.toSet())))
                .entrySet()
                .stream()
                .map(e -> repo.findByOwnerIdAndObjectTypeAndObjectIdIn(
                        e.getKey().ownerId(),
                        e.getKey().favType().getKey(),
                        e.getValue()))
                .<Favorite>mapMulti(Iterable::forEach)
                .collect(toMap(FavKey::from,
                               Function.identity()));
        return keys.stream()
                .map(favsByOwnerType::containsKey)
                .toList();
    }

}
