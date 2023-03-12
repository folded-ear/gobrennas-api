package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class OffsetConnectionCursorCoercing implements Coercing<OffsetConnectionCursor, String> {

    @Override
    public String serialize(@NotNull Object object) {
        if (object instanceof OffsetConnectionCursor) {
            return ((OffsetConnectionCursor) object).getValue();
        }
        throw gonk("serialize", object);
    }

    @Override
    public @NotNull OffsetConnectionCursor parseValue(@NotNull Object input) {
        if (input instanceof String) {
            try {
                return new OffsetConnectionCursor((String) input);
            } catch (RuntimeException re) {
                throw gonk("parse", input, re);
            }
        }
        throw gonk("parse", input);
    }

    @Override
    public @NotNull OffsetConnectionCursor parseLiteral(@NotNull Object literal) {
        if (literal instanceof StringValue) {
            return parseValue(((StringValue) literal).getValue());
        }
        throw gonk("parse", literal);
    }

    private CoercingSerializeException gonk(String action, Object badValue) {
        return gonk(action, badValue, null);
    }

    private CoercingSerializeException gonk(String action, Object badValue, Throwable cause) {
        String msg = String.format("Unable to %s '%s' of type '%s' as Cursor",
                                   action,
                                   badValue,
                                   badValue.getClass().getName());
        return cause == null
                ? new CoercingSerializeException(msg)
                : new CoercingSerializeException(msg, cause);
    }

}
