package com.brennaswitzer.cookbook.util;

import org.junit.Test;

import static com.brennaswitzer.cookbook.util.NumberUtils.parseNumber;
import static com.brennaswitzer.cookbook.util.NumberUtils.parseNumberWithRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class NumberUtilsTest {

    @Test
    public void parseSimpleNames() {
        NumberUtils.NAMES.keySet().forEach(n ->
                assertEquals(NumberUtils.NAMES.get(n), parseNumber(n), 0.001));
    }

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
        assertNull(parseNumber("1-3"));
        assertNull(parseNumber("1.2.3"));
        assertEquals(1.2, parseNumber("1.2.3", true), 0.001);
    }

    @Test
    public void parseWithCoords() {
        NumberUtils.NumberWithRange nwr = parseNumberWithRange("  1 and 2/3 cups fish");
        System.out.println(nwr);
        assertEquals(5.0 / 3.0, nwr.getNumber(), 0.001);
        assertEquals(2, nwr.getStart());
        assertEquals(11, nwr.getEnd());
    }

}