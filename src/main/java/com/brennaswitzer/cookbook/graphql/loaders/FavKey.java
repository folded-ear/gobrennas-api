package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;

public record FavKey(long ownerId, FavoriteType favType, long objectId) {

    public static FavKey from(Favorite f) {
        return new FavKey(f.getOwner().getId(),
                          FavoriteType.parse(f.getObjectType()),
                          f.getObjectId());
    }

}
