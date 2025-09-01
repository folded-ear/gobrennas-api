package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.BatchLoaderRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

@Component
public class IsFavoriteBatchLoader {

    @Autowired
    private FavoriteRepository repo;

    @Autowired
    private BatchLoaderRegistry batchLoaderRegistry;

    @PostConstruct
    void register() {
        batchLoaderRegistry.forTypePair(FavKey.class,
                                        Boolean.class)
                .registerBatchLoader(
                        (keys, env) ->
                                Flux.fromStream(streamInternal(keys)));
    }

    private record OwnerType(long ownerId, FavoriteType favType) {

        public static OwnerType of(FavKey key) {
            return new OwnerType(key.ownerId(), key.favType());
        }

    }

    @VisibleForTesting
    List<Boolean> loadInternal(List<FavKey> keys) {
        return streamInternal(keys)
                .toList();
    }

    private Stream<Boolean> streamInternal(List<FavKey> keys) {
        var favs = keys.stream()
                .collect(groupingBy(OwnerType::of,
                                    mapping(FavKey::objectId,
                                            toSet())))
                .entrySet()
                .stream()
                .map(e -> repo.findByOwnerIdAndObjectTypeAndObjectIdIn(
                        e.getKey().ownerId(),
                        e.getKey().favType().getKey(),
                        e.getValue()))
                .<Favorite>mapMulti(Iterable::forEach)
                .map(FavKey::from)
                .collect(toSet());
        return keys.stream()
                .map(favs::contains);
    }

}
