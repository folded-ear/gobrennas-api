package com.brennaswitzer.cookbook.payload;

import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static com.brennaswitzer.cookbook.payload.RecognizedItem.Range;
import static com.brennaswitzer.cookbook.payload.RecognizedItem.Type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecognizedItemTest {

    @Test
    public void zeroRangeIterator() {
        RecognizedItem el = new RecognizedItem("1 cup flour");
        Iterator<Range> itr = el.unrecognizedWords().iterator();
        assertEquals(new Range(0, 1), itr.next());
        assertEquals(new Range(2, 5), itr.next());
        assertEquals(new Range(6, 11), itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    public void iteratorWithRanges() {
        RecognizedItem el = new RecognizedItem("1 _cup_ flour");
        el.withRange(new Range(2, 5, Type.NEW_UNIT));
        Iterator<Range> itr = el.unrecognizedWords().iterator();
        assertEquals(new Range(0, 1), itr.next());
        assertEquals(new Range(8, 13), itr.next());
        assertFalse(itr.hasNext());
    }

}
