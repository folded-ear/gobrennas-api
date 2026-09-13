package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanBucketTest {

    private static List<PlanBucket> sorted(Comparator<PlanBucket> comparator,
                                           PlanBucket... buckets) {
        List<PlanBucket> list = new ArrayList<>(List.of(buckets));
        Collections.reverse(list);
        list.sort(comparator);
        return list;
    }

    @Test
    void newBucketsAppend() {
        Plan plan = new Plan("plan");

        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        assertEquals(1, a.getPosition());
        assertEquals(2, b.getPosition());
        assertEquals(3, c.getPosition());
    }

    @Test
    void BY_POSITION() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        assertEquals(List.of(a, b, c),
                     sorted(PlanBucket.BY_POSITION, b, c, a));
    }

    @Test
    void BY_DATE() {
        Plan plan = new Plan("plan");
        LocalDate today = LocalDate.now();
        PlanBucket later = new PlanBucket(plan, "later", today.plusDays(1));
        PlanBucket undated = new PlanBucket(plan, "undated", null);
        PlanBucket today1 = new PlanBucket(plan, "today1", today);
        PlanBucket undated2 = new PlanBucket(plan, "undated2", null);
        PlanBucket today2 = new PlanBucket(plan, "today2", today);

        assertEquals(List.of(undated, undated2, today1, today2, later),
                     sorted(PlanBucket.BY_DATE,
                            later,
                            undated,
                            today1,
                            undated2,
                            today2));
    }

    @Test
    void BY_NAME() {
        Plan plan = new Plan("plan");
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket unnamed = new PlanBucket(plan, null, null);
        PlanBucket lowerA = new PlanBucket(plan, "a", null);
        PlanBucket blank = new PlanBucket(plan, "  ", null);
        PlanBucket upperA = new PlanBucket(plan, "A", null);
        PlanBucket empty = new PlanBucket(plan, "", null);

        assertEquals(List.of(unnamed, blank, empty, lowerA, upperA, b),
                     sorted(PlanBucket.BY_NAME,
                            b,
                            unnamed,
                            lowerA,
                            blank,
                            upperA,
                            empty));
    }

}
