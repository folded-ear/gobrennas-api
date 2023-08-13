package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.SlugUtils.toSlug;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlugUtilsTest {

    @Test
    public void happy() {
        assertEquals("empty", toSlug(null));
        assertEquals("empty", toSlug(""));
        assertEquals("empty", toSlug("   \t\t \n  "));
        assertEquals("a", toSlug("a"));
        assertEquals("a", toSlug("A"));
        assertEquals("cat-dog", toSlug("catDog"));
        assertEquals("cat-dog", toSlug("CatDog"));
        assertEquals("team-usa", toSlug("TeamUSA"));
    }

    @Test
    void truncation() {
        assertEquals("team-usa", toSlug("TeamUSA", 10));
        assertEquals("team-usa", toSlug("TeamUSA", 8));
        assertEquals("team-u", toSlug("TeamUSA", 6));
        // no trailing dash!
        assertEquals("team", toSlug("TeamUSA", 5));
    }

}
