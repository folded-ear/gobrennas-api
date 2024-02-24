package com.brennaswitzer.cookbook.payload;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecognizedItemTest {

    @Test
    public void zeroRangeIterator() {
        RecognizedItem el = new RecognizedItem("1 cup flour");
        Iterator<RecognizedRange> itr = el.unrecognizedWords().iterator();
        assertEquals(new RecognizedRange(0, 1), itr.next());
        assertEquals(new RecognizedRange(2, 5), itr.next());
        assertEquals(new RecognizedRange(6, 11), itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    public void iteratorWithRanges() {
        RecognizedItem el = new RecognizedItem("1 _cup_ flour");
        el.withRange(new RecognizedRange(2, 5, RecognizedRangeType.NEW_UNIT));
        Iterator<RecognizedRange> itr = el.unrecognizedWords().iterator();
        assertEquals(new RecognizedRange(0, 1), itr.next());
        assertEquals(new RecognizedRange(8, 13), itr.next());
        assertFalse(itr.hasNext());
    }

}
