package com.brennaswitzer.cookbook.domain;

import lombok.NonNull;

public class NoConversionException extends RuntimeException {

    public NoConversionException(UnitOfMeasure from, UnitOfMeasure to) {
        super("No conversion exists from '" + format(from) + "' to '" + format(to) + "'.");
    }

    @NonNull
    private static String format(UnitOfMeasure uom) {
        return uom == null ? "count/each" : uom.getName();
    }

}
