package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

public enum FavoriteType {
    RECIPE(Recipe.class);

    @Getter
    private final String key;

    FavoriteType(Class<? extends BaseEntity> clazz) {
        this(clazz.getSimpleName());
    }

    FavoriteType(String key) {
        this.key = key;
    }

    public boolean matches(String key) {
        return this.key.equals(key);
    }

}
