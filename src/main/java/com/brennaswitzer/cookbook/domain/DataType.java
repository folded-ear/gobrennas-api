package com.brennaswitzer.cookbook.domain;

public enum DataType implements Identified {
    JSON(1L);

    private final Long id;

    DataType(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }
}
