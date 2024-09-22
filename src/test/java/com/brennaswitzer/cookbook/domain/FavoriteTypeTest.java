package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FavoriteTypeTest {

    @Test
    void matches() {
        assertTrue(FavoriteType.RECIPE.matches("Recipe"));
        assertFalse(FavoriteType.RECIPE.matches("recipe"));
        assertFalse(FavoriteType.RECIPE.matches("RECIPE"));
    }

    @Test
    void parse() {
        assertEquals(FavoriteType.RECIPE, FavoriteType.parse("Recipe"));
        assertThrows(IllegalArgumentException.class,
                     () -> FavoriteType.parse(""));
        assertThrows(IllegalArgumentException.class,
                     () -> FavoriteType.parse("recipe"));
        assertThrows(IllegalArgumentException.class,
                     () -> FavoriteType.parse("RECIPE"));
    }

}
