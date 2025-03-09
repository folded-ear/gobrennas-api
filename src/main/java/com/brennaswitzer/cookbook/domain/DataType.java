package com.brennaswitzer.cookbook.domain;

public enum DataType implements Identified {
    /**
     * Arbitrary JSON-serialized data
     */
    JSON(1L),
    BOOLEAN(2L),
    STRING(3L),
    /**
     * A {@link #STRING} with ID semantics.
     */
    ID(4L),
    /**
     * A JSON-serialized list of {@link #ID} values
     */
    SET_OF_IDS(5L),
    INT(6L),
    FLOAT(7L),
    ;

    private final Long id;

    DataType(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
