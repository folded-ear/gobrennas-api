package com.brennaswitzer.cookbook.payload;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecognizedRangeTest {

    @Test
    void overlaps() {
        var a = new RecognizedRange(2, 4);
        // surrounds
        assertTrue(a.overlaps(new RecognizedRange(0, 6)));
        assertTrue(new RecognizedRange(0, 6).overlaps(a));
        // equal
        assertTrue(a.overlaps(new RecognizedRange(2, 4)));
        // within
        assertTrue(a.overlaps(new RecognizedRange(3, 3)));
        assertTrue(new RecognizedRange(3, 3).overlaps(a));
        // start
        assertTrue(a.overlaps(new RecognizedRange(0, 3)));
        assertTrue(new RecognizedRange(0, 3).overlaps(a));
        assertFalse(a.overlaps(new RecognizedRange(0, 2)));
        assertFalse(new RecognizedRange(0, 2).overlaps(a));
        assertFalse(a.overlaps(new RecognizedRange(0, 1)));
        assertFalse(new RecognizedRange(0, 1).overlaps(a));
        // end
        assertTrue(a.overlaps(new RecognizedRange(3, 6)));
        assertTrue(new RecognizedRange(3, 6).overlaps(a));
        assertFalse(a.overlaps(new RecognizedRange(4, 6)));
        assertFalse(new RecognizedRange(4, 6).overlaps(a));
        assertFalse(a.overlaps(new RecognizedRange(5, 6)));
        assertFalse(new RecognizedRange(5, 6).overlaps(a));
    }

}
