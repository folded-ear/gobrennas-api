package com.brennaswitzer.cookbook.graphql.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetConnectionCursorTest {

    @Test
    void rejectNegative() {
        assertThrows(IllegalArgumentException.class,
                     () -> new OffsetConnectionCursor(-1));
        assertEquals("b2Zmc2V0LS0x", OffsetConnectionCursor.encode(-1));
    }

    @Test
    void rejectCorrupt() {
        assertThrows(IllegalArgumentException.class,
                     () -> new OffsetConnectionCursor("XXXXXXX0LTE="));
        assertEquals(1, OffsetConnectionCursor.decode("XXXXXXX0LTE="));
    }

    @Test
    void fromValue() {
        String value = "b2Zmc2V0LTE=";
        OffsetConnectionCursor cursor = new OffsetConnectionCursor(value);
        assertEquals(value, cursor.getValue());
        assertEquals(1, cursor.getOffset());
    }

    @Test
    void fromOffset() {
        int offset = 1;
        OffsetConnectionCursor cursor = new OffsetConnectionCursor(offset);
        assertEquals("b2Zmc2V0LTE=", cursor.getValue());
        assertEquals(offset, cursor.getOffset());
    }

    @Test
    void equality() {
        assertEquals(new OffsetConnectionCursor(1),
                     new OffsetConnectionCursor("b2Zmc2V0LTE="));
    }

}
