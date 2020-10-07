package com.brennaswitzer.cookbook.util;

import org.springframework.util.Assert;

public class SlugUtils {

    public static String toSlug(String name) {
        Assert.notNull(name, "Null cannot be converted to a slug");
        if (name.length() == 0) return name;
        return name.substring(0, 1).toLowerCase() +
                name.substring(1)
                .replaceAll("([A-Z0-9]+)", "-$1")
                .replaceAll("[^a-zA-Z0-9]+", "-")
                .toLowerCase();
    }

}
