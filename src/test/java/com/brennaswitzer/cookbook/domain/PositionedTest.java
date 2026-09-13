package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PositionedTest {

    @Getter
    @Setter
    private static class Thing implements Positioned {

        private final String name;
        private int position;

        Thing(String name, int position) {
            this.name = name;
            this.position = position;
        }

        @Override
        public String toString() {
            return name + "@" + position;
        }

    }

    private static void assertOrder(Thing... things) {
        for (int i = 1; i < things.length; i++) {
            assertTrue(things[i - 1].getPosition() < things[i].getPosition(),
                       List.of(things).toString());
        }
    }

    @Test
    void nextPosition_empty() {
        assertEquals(1, Positioned.nextPosition(List.of()));
    }

    @Test
    void nextPosition() {
        List<Thing> peers = List.of(new Thing("a", 3), new Thing("b", 1));

        assertEquals(4, Positioned.nextPosition(peers));
    }

    @Test
    void insertAt_first() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 2);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b), s, 0);

        assertOrder(s, a, b);
        assertEquals(1, a.getPosition());
        assertEquals(2, b.getPosition());
    }

    @Test
    void insertAt_middle() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 2);
        Thing c = new Thing("c", 3);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b, c), s, 2);

        assertOrder(a, s, b, c);
        assertEquals(1, a.getPosition());
        assertEquals(2, s.getPosition());
    }

    @Test
    void insertAt_end() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 2);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b), s, 3);

        assertOrder(a, b, s);
        assertEquals(1, a.getPosition());
        assertEquals(2, b.getPosition());
    }

    @Test
    void insertAt_pastEnd() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 2);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b), s, 10);

        assertOrder(a, b, s);
    }

    @Test
    void insertAt_keepsGaps() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 5);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b), s, 2);

        assertOrder(a, s, b);
        assertEquals(5, b.getPosition());
    }

    @Test
    void insertAt_resolvesDuplicates() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 1);
        Thing s = new Thing("s", 0);

        Positioned.insertAt(List.of(a, b), s, 2);

        assertOrder(a, s, b);
    }

    @Test
    void insertAt_subjectAmongPeers_later() {
        Thing a = new Thing("a", 1);
        Thing s = new Thing("s", 2);
        Thing b = new Thing("b", 3);

        Positioned.insertAt(List.of(a, s, b), s, 4);

        assertOrder(a, b, s);
    }

    @Test
    void insertAt_subjectAmongPeers_earlier() {
        Thing a = new Thing("a", 1);
        Thing b = new Thing("b", 2);
        Thing s = new Thing("s", 3);

        Positioned.insertAt(List.of(a, b, s), s, 0);

        assertOrder(s, a, b);
    }

}
