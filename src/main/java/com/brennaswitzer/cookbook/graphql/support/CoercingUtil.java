package com.brennaswitzer.cookbook.graphql.support;

import graphql.schema.CoercingSerializeException;

public final class CoercingUtil {

    private CoercingUtil() {
        throw new UnsupportedOperationException("really?");
    }

    public static CoercingSerializeException coercionFailure(String action, Object badValue) {
        return coercionFailure(action, badValue, null);
    }

    public static CoercingSerializeException coercionFailure(String action, Object badValue, Throwable cause) {
        String msg = String.format("Unable to %s '%s' of type '%s' as Cursor",
                                   action,
                                   badValue,
                                   badValue.getClass().getName());
        return cause == null
                ? new CoercingSerializeException(msg)
                : new CoercingSerializeException(msg, cause);
    }

}
