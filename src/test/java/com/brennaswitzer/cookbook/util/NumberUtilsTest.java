package com.brennaswitzer.cookbook.util;

import org.junit.Test;

import static com.brennaswitzer.cookbook.util.NumberUtils.parseNumber;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NumberUtilsTest {

    @Test
    public void parseDecimal() {
        assertEquals(1, parseNumber("1"), 0.001);
        assertEquals(1, parseNumber("1.0"), 0.001);
        assertEquals(0.1, parseNumber("0.1"), 0.001);
        assertEquals(0.01, parseNumber("0.01"), 0.001);
        assertEquals(0.01, parseNumber("0.01"), 0.001);
        assertEquals(1.23, parseNumber("1.23"), 0.001);
    }

    @Test
    public void parseFraction() {
        assertEquals(0.5, parseNumber("1/2"), 0.001);
        assertEquals(0.5, parseNumber("1/ 2"), 0.001);
        assertEquals(0.5, parseNumber("1 /2"), 0.001);
        assertEquals(0.5, parseNumber("1 / 2"), 0.001);
        assertEquals(1.5, parseNumber("3 / 2"), 0.001);

        assertEquals(0.333, parseNumber("1/3"), 0.001);
        assertEquals(0.667, parseNumber("2/3"), 0.001);
    }

    @Test
    public void parseFractionGlyph() {
        assertEquals(0.25, parseNumber("¼"), 0.001);
        assertEquals(0.5, parseNumber("½"), 0.001);
        assertEquals(0.75, parseNumber("¾"), 0.001);
        assertEquals(0.143, parseNumber("⅐"), 0.001);
        assertEquals(0.111, parseNumber("⅑"), 0.001);
        assertEquals(0.1, parseNumber("⅒"), 0.001);
        assertEquals(0.333, parseNumber("⅓"), 0.001);
        assertEquals(0.667, parseNumber("⅔"), 0.001);
        assertEquals(0.2, parseNumber("⅕"), 0.001);
        assertEquals(0.4, parseNumber("⅖"), 0.001);
        assertEquals(0.6, parseNumber("⅗"), 0.001);
        assertEquals(0.8, parseNumber("⅘"), 0.001);
        assertEquals(0.167, parseNumber("⅙"), 0.001);
        assertEquals(0.833, parseNumber("⅚"), 0.001);
        assertEquals(0.125, parseNumber("⅛"), 0.001);
        assertEquals(0.375, parseNumber("⅜"), 0.001);
        assertEquals(0.625, parseNumber("⅝"), 0.001);
        assertEquals(0.875, parseNumber("⅞"), 0.001);
    }

    @Test
    public void parseProperFraction() {
        assertEquals(3.5, parseNumber("3&1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 & 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 and 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 1/2"), 0.001);
        assertEquals(3.5, parseNumber("3 ½"), 0.001);
        assertEquals(3.5, parseNumber("3½"), 0.001);
        assertEquals(3.5, parseNumber("3&½"), 0.001);
        assertEquals(3.5, parseNumber("3 & ½"), 0.001);
        assertEquals(3.5, parseNumber("3 and ½"), 0.001);
    }

    @Test
    public void parseName() {
        assertEquals(1, parseNumber("one"), 0.001);
        assertEquals(2, parseNumber("two"), 0.001);
        assertEquals(3, parseNumber("three"), 0.001);
        assertEquals(4, parseNumber("four"), 0.001);
        assertEquals(5, parseNumber("five"), 0.001);
        assertEquals(6, parseNumber("six"), 0.001);
        assertEquals(7, parseNumber("seven"), 0.001);
        assertEquals(8, parseNumber("eight"), 0.001);
        assertEquals(9, parseNumber("nine"), 0.001);
        assertEquals(10, parseNumber("ten"), 0.001);
        assertEquals(11, parseNumber("eleven"), 0.001);
        assertEquals(12, parseNumber("twelve"), 0.001);
        assertEquals(13, parseNumber("thirteen"), 0.001);
        assertEquals(14, parseNumber("fourteen"), 0.001);
        assertEquals(15, parseNumber("fifteen"), 0.001);
        assertEquals(16, parseNumber("sixteen"), 0.001);
        assertEquals(17, parseNumber("seventeen"), 0.001);
        assertEquals(18, parseNumber("eighteen"), 0.001);
        assertEquals(19, parseNumber("nineteen"), 0.001);
        assertEquals(20, parseNumber("twenty"), 0.001);

        assertEquals(0.5, parseNumber("half"), 0.001);
        assertEquals(0.5, parseNumber("one half"), 0.001);
        assertEquals(2.5, parseNumber("two & one half"), 0.001);
        assertEquals(2.5, parseNumber("two and one half"), 0.001);
    }

    @Test
    public void parseBadThings() {
        assertNull(parseNumber(null));
        assertNull(parseNumber(""));
        assertNull(parseNumber("    "));
        assertNull(parseNumber("glergypants"));
        assertNull(parseNumber("1.2.3"));
        assertNull(parseNumber("1-3"));
    }

}