package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

@Getter
public enum PlanItemStatus implements Identified {

    NEEDED(0L),
    ACQUIRED(50L),
    COMPLETED(100L, true),
    DELETED(-1L, true);

    private final Long id;
    private final boolean forDelete;

    PlanItemStatus(Long id) {
        this(id, false);
    }

    PlanItemStatus(Long id, boolean forDelete) {
        this.id = id;
        this.forDelete = forDelete;
    }

}
