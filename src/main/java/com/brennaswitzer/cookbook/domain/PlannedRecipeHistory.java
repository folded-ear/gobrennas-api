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
public class PlannedRecipeHistory extends BaseEntity implements Owned {

    public static final Comparator<PlannedRecipeHistory> BY_RECENT = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return b.getDoneAt().compareTo(a.getDoneAt());
    };

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Recipe recipe;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User owner;

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
    @Column(updatable = false)
    private Instant doneAt;

    @NotNull
    @Column(name = "status_id",
            updatable = false)
    private PlanItemStatus status;

    private Rating rating;

    private String notes;

}
