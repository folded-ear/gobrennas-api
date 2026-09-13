package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import java.util.Comparator;

@Getter
public enum PlanBucketSort {

    POSITION(PlanBucket.BY_POSITION),
    DATE(PlanBucket.BY_DATE),
    NAME(PlanBucket.BY_NAME);

    private final Comparator<PlanBucket> comparator;

    PlanBucketSort(Comparator<PlanBucket> comparator) {
        this.comparator = comparator;
    }

}
