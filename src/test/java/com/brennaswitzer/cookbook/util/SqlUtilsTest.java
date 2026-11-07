package com.brennaswitzer.cookbook.util;

import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.SqlUtils.escapeLike;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SqlUtilsTest {

    @Test
    void escapeLikeEscapesMetacharacters() {
        assertEquals("100\\%", escapeLike("100%"));
        assertEquals("a\\_b", escapeLike("a_b"));
        assertEquals("c:\\\\temp", escapeLike("c:\\temp"));
        assertEquals("\\\\\\%\\_", escapeLike("\\%_"));
    }

    @Test
    void escapeLikeLeavesOthersAlone() {
        assertNull(escapeLike(null));
        assertEquals("", escapeLike(""));
        assertEquals("Bob's [cool] *pie*?", escapeLike("Bob's [cool] *pie*?"));
    }

}
