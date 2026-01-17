package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.payload.RawIngredientDissection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RawUtilsTest {

    @Test
    public void longestSuffix() {
        assertEquals(0, RawUtils.lengthOfLongestSharedSuffix("", ""));
        assertEquals(0, RawUtils.lengthOfLongestSharedSuffix("abc", "xyz"));
        assertEquals(1, RawUtils.lengthOfLongestSharedSuffix("a", "a"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("abc", "abc"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("abc", "zzzzabc"));
        assertEquals(3, RawUtils.lengthOfLongestSharedSuffix("zzzzabc", "abc"));
    }

    @Test
    void lastIndexOfName_normalQuotes() {
        assertEquals(-1, RawUtils.lastIndexOfNameStart("1 goat, shredded"));
        assertEquals(-1, RawUtils.lastIndexOfNameStart("1 goat, shredded", 10));
        assertEquals(-1, RawUtils.lastIndexOfNameStart("1 goat, shredded", 0));
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 \"goat\", shredded"));
        assertEquals(2, RawUtils.lastIndexOfNameStart("1 \"goat\", shredded", 6));
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 \"goat\", shredded", 7));
    }

    @Test
    void lastIndexOfName_curlyQuotes() {
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 “goat”, shredded"));
        assertEquals(2, RawUtils.lastIndexOfNameStart("1 “goat”, shredded", 6));
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 “goat”, shredded", 7));
    }

    @Test
    void lastIndexOfName_angleQuotes() {
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 «goat», shredded"));
        assertEquals(2, RawUtils.lastIndexOfNameStart("1 «goat», shredded", 6));
        assertEquals(7, RawUtils.lastIndexOfNameStart("1 «goat», shredded", 7));
    }

    @Test
    void containsNameDelim() {
        assertFalse(RawUtils.containsNameDelim("1 goat, shredded"));
        assertTrue(RawUtils.containsNameDelim("1 goat\", shredded"));
        assertTrue(RawUtils.containsNameDelim("1 “goat, shredded"));
        assertTrue(RawUtils.containsNameDelim("1 goat”, shredded"));
        assertTrue(RawUtils.containsNameDelim("1 «goat, shredded"));
        assertTrue(RawUtils.containsNameDelim("1 goat», shredded"));
    }

    @Test
    void stripMarkers() {
        assertNull(RawUtils.stripMarkers(null));
        assertEquals("", RawUtils.stripMarkers(""));
        assertEquals("goat", RawUtils.stripMarkers("goat"));
        assertEquals("goat", RawUtils.stripMarkers("\"goat\""));
        assertEquals("goat", RawUtils.stripMarkers("“goat”"));
        assertEquals("goat", RawUtils.stripMarkers("«goat»"));
        assertEquals("grams", RawUtils.stripMarkers("_grams_"));
        assertEquals("goat\"", RawUtils.stripMarkers("goat\""));
        assertEquals("goat”", RawUtils.stripMarkers("goat”"));
        assertEquals("goat»", RawUtils.stripMarkers("goat»"));
        assertEquals("grams_", RawUtils.stripMarkers("grams_"));
        assertEquals("\"goat", RawUtils.stripMarkers("\"goat"));
        assertEquals("“goat", RawUtils.stripMarkers("“goat"));
        assertEquals("«goat", RawUtils.stripMarkers("«goat"));
        assertEquals("_grams", RawUtils.stripMarkers("_grams"));
    }

    @ParameterizedTest
    @MethodSource
    public void fromTestFile(RawIngredientDissection expected) {
        assertEquals(expected, RawUtils.dissect(expected.getRaw()));
    }

    private static Stream<RawIngredientDissection> fromTestFile() throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(RawUtilsTest.class.getResourceAsStream(
                "/raw/dissections.txt")));
        r.readLine(); // the header
        return r.lines()
                .filter(l -> !l.isBlank())
                .filter(l -> !l.startsWith("#"))
                .filter(l -> !l.startsWith("//"))
                .map(l -> l.split("\\|"))
                .map(RawUtilsTest::inflateDissection);
    }

    private static RawIngredientDissection inflateDissection(String[] parts) {
        RawIngredientDissection dissection = new RawIngredientDissection(parts[0]);
        if (parts.length > 1 && !parts[1].isEmpty()) {
            dissection.setQuantity(new RawIngredientDissection.Section(
                    parts[1],
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            ));
        }
        if (parts.length > 4 && !parts[4].isEmpty()) {
            dissection.setUnits(new RawIngredientDissection.Section(
                    parts[4],
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6])
            ));
        }
        if (parts.length > 7 && !parts[7].isEmpty()) {
            dissection.setName(new RawIngredientDissection.Section(
                    parts[7],
                    Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9])
            ));
        }
        return dissection;
    }
}
