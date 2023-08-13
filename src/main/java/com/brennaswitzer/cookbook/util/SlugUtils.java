package com.brennaswitzer.cookbook.util;

public class SlugUtils {

    public static String toSlug(String name) {
        return toSlug(name, -1);
    }

    public static String toSlug(String name, int maxLength) {
        if (name == null || name.isBlank()) return "empty";
        String slug = name.substring(0, 1).toLowerCase() +
                name.substring(1)
                        .replaceAll("([A-Z0-9]+)", "-$1")
                        .replaceAll("[^a-zA-Z0-9]+", "-")
                        .toLowerCase();
        if (maxLength > 0 && slug.length() > maxLength) {
            slug = slug.substring(0, maxLength);
            while (slug.endsWith("-")) {
                slug = slug.substring(0, slug.length() - 1);
            }
        }
        return slug;
    }

}
