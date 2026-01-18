package com.brennaswitzer.cookbook.payload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @ParameterizedTest
    @MethodSource
    void merge(RecognizedRange left, RecognizedRange right, RecognizedRange expected) {
        assertEquals(expected,
                     left.merge(right),
                     () -> String.format("expected merge of %d-%d and %d-%d to be %d-%d",
                                         left.getStart(), left.getEnd(),
                                         right.getStart(), right.getEnd(),
                                         expected.getStart(), expected.getEnd()));
    }

    private static Stream<Arguments> merge() {
        RecognizedRange a = new RecognizedRange(2, 6);
        return Stream.of(
                Arguments.of(a,
                             new RecognizedRange(3, 5),
                             a),
                Arguments.of(a,
                             new RecognizedRange(1, 5),
                             new RecognizedRange(1, 6)),
                Arguments.of(a,
                             new RecognizedRange(3, 7),
                             new RecognizedRange(2, 7)),
                Arguments.of(new RecognizedRange(3, 5),
                             a,
                             a),
                Arguments.of(new RecognizedRange(4, 6),
                             new RecognizedRange(2, 4),
                             a),
                Arguments.of(new RecognizedRange(2, 4),
                             new RecognizedRange(4, 6),
                             a)
        );
    }

}
