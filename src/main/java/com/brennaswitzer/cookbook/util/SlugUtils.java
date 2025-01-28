package com.brennaswitzer.cookbook.util;

import java.util.List;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final List<Munge> MUNGES = List.of(
            new Munge("\\Bn't\\b", "nt"),
            new Munge("(\\w)'s\\b", "$1s"),
            new Munge("([A-Z0-9]+)", "-$1"),
            new Munge("[^a-zA-Z0-9]+", "-"),
            new Munge("-+", "-"),
            new Munge("^-|-$", ""));

    private record Munge(Pattern pat, String repl) {

        Munge(String pat, String repl) {
            this(Pattern.compile(pat), repl);
        }

        public String munge(String slug) {
            return pat.matcher(slug).replaceAll(repl);
        }

    }

    public static String toSlug(String name) {
        return toSlug(name, -1);
    }

    public static String toSlug(String name, int maxLength) {
        if (name == null || name.isBlank()) return "empty";
        String slug = name;
        for (Munge m : MUNGES) {
            slug = m.munge(slug);
        }
        slug = slug.toLowerCase();
        if (maxLength > 0 && slug.length() > maxLength) {
            slug = slug.substring(0, maxLength);
            if (slug.endsWith("-")) {
                slug = slug.substring(0, slug.length() - 1);
            }
        }
        return slug;
    }

}
