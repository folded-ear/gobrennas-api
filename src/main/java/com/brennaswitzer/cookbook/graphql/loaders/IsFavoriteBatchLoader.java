package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Controller
public class IsFavoriteBatchLoader {

    @Autowired
    private FavoriteRepository repo;

    private record OwnerType(long ownerId, FavoriteType favType) {

        public static OwnerType of(FavKey key) {
            return new OwnerType(key.ownerId(), key.favType());
        }

    }

    @BatchMapping(typeName = "Recipe", field = "favorite")
    public Map<Recipe, Boolean> recipeFavorite(List<Recipe> recipes,
                                               java.security.Principal principal) {
        return favorite(FavoriteType.RECIPE,
                        recipes,
                        principal);
    }

    private <T extends Identified> Map<T, Boolean> favorite(FavoriteType favType,
                                                            List<T> items,
                                                            java.security.Principal principal) {
        // todo: there's got to be a better way to get the userId, but
        //  @AuthenticationPrincipal only works on @SchemaMapping, not @BatchMapping...
        Long userId = ((UserPrincipal) ((Authentication) principal).getPrincipal()).getId();
        List<FavKey> keys = items.stream()
                .map(it -> new FavKey(userId,
                                      favType,
                                      it.getId()))
                .toList();
        Iterator<T> itr = items.iterator();
        return streamInternal(keys)
                .collect(toMap(f -> itr.next(),
                               Function.identity()));
    }

    @VisibleForTesting
    List<Boolean> loadInternal(List<FavKey> keys) {
        return streamInternal(keys)
                .toList();
    }

    private Stream<Boolean> streamInternal(List<FavKey> keys) {
        var favsByOwnerType = keys.stream()
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
                .map(favsByOwnerType::contains);
    }

}
