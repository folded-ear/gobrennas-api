package com.brennaswitzer.cookbook.domain.measure;

public class NoConversionException extends RuntimeException {

    public NoConversionException(UnitOfMeasure from, UnitOfMeasure to) {
        super("No conversion exists from '" + from.getName() + "' to '" + to.getName() + "'.");
    }

}
