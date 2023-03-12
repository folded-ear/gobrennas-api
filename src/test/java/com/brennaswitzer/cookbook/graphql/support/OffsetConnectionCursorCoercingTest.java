package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import graphql.language.StringValue;
import graphql.schema.CoercingSerializeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OffsetConnectionCursorCoercingTest {

    private OffsetConnectionCursorCoercing coercing;

    @BeforeEach
    public void setUp() {
        coercing = new OffsetConnectionCursorCoercing();
    }

    @Test
    public void serialize() {
        assertEquals("b2Zmc2V0LTE=",
                     coercing.serialize(new OffsetConnectionCursor(1)));
    }

    @ParameterizedTest
    @CsvSource({
            "garbage",
            "XXXXXXX0LTE="
    })
    public void parseBadValue(String input) {
        assertThrows(CoercingSerializeException.class,
                     () -> coercing.parseLiteral(input));
    }

    @Test
    public void parseGoodValue() {
        assertEquals(new OffsetConnectionCursor(1),
                     coercing.parseValue("b2Zmc2V0LTE="));
    }

    @Test
    public void parseLiteral() {
        assertEquals(new OffsetConnectionCursor(1),
                     coercing.parseLiteral(new StringValue("b2Zmc2V0LTE=")));
    }

}
