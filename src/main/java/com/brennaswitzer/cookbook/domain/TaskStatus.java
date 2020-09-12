package com.brennaswitzer.cookbook.domain;

public enum TaskStatus implements Identified {

    NEEDED(0L),
    ACQUIRED(50L),
    COMPLETED(100L),
    DELETED(-1L);

    private final Long id;

    TaskStatus(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

}
