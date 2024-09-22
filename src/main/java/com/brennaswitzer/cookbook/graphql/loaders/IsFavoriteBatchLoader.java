package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.google.common.annotations.VisibleForTesting;
import org.dataloader.BatchLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;

@Component
public class IsFavoriteBatchLoader implements BatchLoader<IsFavorite, Boolean> {

    @Autowired
    private FavoriteRepository repo;

    private record OwnerType(long ownerId, FavoriteType favType) {

        public static OwnerType of(IsFavorite key) {
            return new OwnerType(key.ownerId(), key.favType());
        }

        public static OwnerType of(Favorite fav) {
            return new OwnerType(fav.getOwner().getId(), FavoriteType.parse(fav.getObjectType()));
        }

    }

    @Override
    public CompletionStage<List<Boolean>> load(List<IsFavorite> keys) {
        return CompletableFuture.supplyAsync(() -> loadInternal(keys));
    }

    @VisibleForTesting
    List<Boolean> loadInternal(List<IsFavorite> keys) {
        var favIdsByOwnerType = keys.stream()
                .collect(groupingBy(OwnerType::of,
                                    mapping(IsFavorite::objectId,
                                            Collectors.toSet())))
                .entrySet()
                .stream()
                .map(e -> repo.findByOwnerIdAndObjectTypeAndObjectIdIn(
                        e.getKey().ownerId(),
                        e.getKey().favType().getKey(),
                        e.getValue()))
                .<Favorite>mapMulti(Iterable::forEach)
                .collect(groupingBy(OwnerType::of,
                                    mapping(Favorite::getObjectId,
                                            Collectors.toSet())));
        return keys.stream()
                .map(k -> favIdsByOwnerType.getOrDefault(
                                OwnerType.of(k),
                                Set.of())
                        .contains(k.objectId()))
                .toList();
    }

}
