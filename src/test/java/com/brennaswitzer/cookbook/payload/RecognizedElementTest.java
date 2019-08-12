package com.brennaswitzer.cookbook.payload;

import org.junit.Test;

import java.util.Iterator;

import static com.brennaswitzer.cookbook.payload.RecognizedElement.Range;
import static com.brennaswitzer.cookbook.payload.RecognizedElement.Type;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RecognizedElementTest {

    @Test
    public void zeroRangeIterator() {
        RecognizedElement el = new RecognizedElement("1 cup flour");
        Iterator<Range> itr = el.unrecognizedWords().iterator();
        assertEquals(new Range(0, 1), itr.next());
        assertEquals(new Range(2, 5), itr.next());
        assertEquals(new Range(6, 11), itr.next());
        assertFalse(itr.hasNext());
    }

    @Test
    public void iteratorWithRanges() {
        RecognizedElement el = new RecognizedElement("1 _cup_ flour");
        el.withRange(new Range(2, 5, Type.NEW_UNIT));
        Iterator<Range> itr = el.unrecognizedWords().iterator();
        assertEquals(new Range(0, 1), itr.next());
        assertEquals(new Range(8, 13), itr.next());
        assertFalse(itr.hasNext());
    }

}