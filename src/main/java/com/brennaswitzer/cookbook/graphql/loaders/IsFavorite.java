package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.FavoriteType;

public record IsFavorite(long ownerId, FavoriteType favType, long objectId) {}
