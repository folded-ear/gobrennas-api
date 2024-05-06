package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Comparator;

@Setter
@Getter
@Entity
public class PlannedRecipeHistory extends BaseEntity {

    public static final Comparator<PlannedRecipeHistory> BY_RECENT = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return b.getDoneAt().compareTo(a.getDoneAt());
    };

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Recipe recipe;

    /**
     * The ID of the plan item this recipe was planned as.
     */
    @NotNull
    @Column(updatable = false)
    private Long planItemId;

    @NotNull
    @Column(updatable = false)
    private Instant plannedAt;

    @NotNull
    @Column(name = "status_id",
            updatable = false)
    private PlanItemStatus status;

    public Instant getDoneAt() {
        return getCreatedAt();
    }

}
