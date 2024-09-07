package com.brennaswitzer.cookbook.graphql.model;

import com.google.common.annotations.VisibleForTesting;
import graphql.relay.ConnectionCursor;
import lombok.Value;

import java.util.Base64;
import java.util.Objects;

@Value
public class OffsetConnectionCursor implements ConnectionCursor {

    private static final String PREFIX = "offset-";

    @VisibleForTesting
    static int decode(String value) {
        return Integer.parseInt(
                new String(Base64.getDecoder()
                                   .decode(value))
                        .substring(PREFIX.length()));
    }

    @VisibleForTesting
    static String encode(int offset) {
        return Base64.getEncoder()
                .encodeToString((PREFIX + offset).getBytes());
    }

    int offset;

    String value;

    // for Jackson conversion
    @SuppressWarnings("unused")
    private OffsetConnectionCursor(int offset, String value) {
        this.offset = offset;
        this.value = value;
        if (decode(value) != offset) {
            throw new IllegalArgumentException(String.format(
                    "Inconsistent %d and '%s' cursor values",
                    offset,
                    value));
        }
    }

    public OffsetConnectionCursor(String value) {
        this(decode(value));
        if (!Objects.equals(value, this.value)) {
            throw new IllegalArgumentException(String.format(
                    "Invalid '%s' cursor value",
                    value));
        }
    }

    public OffsetConnectionCursor(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException(String.format(
                    "Offsets cannot be negative, but '%d' is",
                    offset));
        }
        this.offset = offset;
        this.value = encode(offset);
    }

}
