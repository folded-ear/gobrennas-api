package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlanBucketTest {

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

}
