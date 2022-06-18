package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.SlugUtils.toSlug;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SlugUtilsTest {

    @Test
    public void happy() {
        assert "".equals(toSlug(""));
        assert "a".equals(toSlug("a"));
        assert "a".equals(toSlug("A"));
        assert "cat-dog".equals(toSlug("catDog"));
        assert "cat-dog".equals(toSlug("CatDog"));
        assert "team-usa".equals(toSlug("TeamUSA"));
    }

    @Test
    public void refuseNull() {
        assertThrows(IllegalArgumentException.class, () ->
                toSlug(null));
    }

}
