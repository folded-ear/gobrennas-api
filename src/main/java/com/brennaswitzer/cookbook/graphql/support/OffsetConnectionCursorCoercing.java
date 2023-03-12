package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.graphql.model.OffsetConnectionCursor;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static com.brennaswitzer.cookbook.graphql.support.CoercingUtil.coercionFailure;

@Component
public class OffsetConnectionCursorCoercing implements Coercing<OffsetConnectionCursor, String> {

    @Override
    public String serialize(@NotNull Object object) {
        if (object instanceof OffsetConnectionCursor) {
            return ((OffsetConnectionCursor) object).getValue();
        }
        throw coercionFailure("serialize", object);
    }

    @Override
    public @NotNull OffsetConnectionCursor parseValue(@NotNull Object input) {
        if (input instanceof String) {
            try {
                return new OffsetConnectionCursor((String) input);
            } catch (RuntimeException re) {
                throw coercionFailure("parse", input, re);
            }
        }
        throw coercionFailure("parse", input);
    }

    @Override
    public @NotNull OffsetConnectionCursor parseLiteral(@NotNull Object literal) {
        if (literal instanceof StringValue) {
            return parseValue(((StringValue) literal).getValue());
        }
        throw coercionFailure("parse", literal);
    }

}
