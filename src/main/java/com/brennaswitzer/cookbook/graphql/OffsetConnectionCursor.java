package com.brennaswitzer.cookbook.graphql;

import graphql.relay.ConnectionCursor;
import lombok.Value;

import java.util.Base64;
import java.util.Objects;

@Value
public class OffsetConnectionCursor implements ConnectionCursor {

    private static final String PREFIX = "offset-";

    private static int decode(String value) {
        return Integer.parseInt(
                new String(Base64.getDecoder()
                                   .decode(value))
                        .substring(PREFIX.length()));
    }

    private static String encode(int offset) {
        return Base64.getEncoder()
                .encodeToString((PREFIX + offset).getBytes());
    }

    int offset;

    String value;

    @SuppressWarnings("unused") // reflected by graphql-java
    public OffsetConnectionCursor(String value) {
        this(decode(value));
        if (!Objects.equals(value, this.value)) {
            throw new IllegalArgumentException(String.format("Invalid '%s' cursor value",
                                                             value));
        }
    }

    public OffsetConnectionCursor(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException(String.format("Offsets cannot be negative, but '%d' is",
                                                             offset));
        }
        this.offset = offset;
        this.value = encode(offset);
    }

    @Override
    public String toString() {
        return value; // todo: add a Coercing to the scalar type so this can be normal?
    }

}
