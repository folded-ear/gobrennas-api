package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public enum FavoriteType {
    RECIPE(Recipe.class),
    ;

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

    public static FavoriteType parse(String key) {
        for (var v : values())
            if (v.matches(key)) return v;
        throw new IllegalArgumentException(String.format(
                "No '%s' %s",
                key,
                FavoriteType.class.getSimpleName()));
    }

}
