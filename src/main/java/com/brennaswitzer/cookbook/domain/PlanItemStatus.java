package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

@Getter
public enum PlanItemStatus implements Identified {

    NEEDED(0L),
    ACQUIRED(50L),
    COMPLETED(100L),
    DELETED(-1L);

    private final Long id;

    PlanItemStatus(Long id) {
        this.id = id;
    }

    public boolean isForDelete() {
        return COMPLETED.equals(this) || DELETED.equals(this);
    }
}
