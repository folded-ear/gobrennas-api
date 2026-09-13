package com.brennaswitzer.cookbook.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlanTest {

    @Test
    public void owner() {
        Plan plan = new Plan();
        assertNull(plan.getOwner());
        User u = new User();
        plan.setOwner(u);
        assertSame(u, plan.getOwner());
    }

    @Test
    public void assigneeIsOwner() {
        Plan plan = new Plan();
        assertNull(plan.getAssignee());
        User u = new User();
        plan.setOwner(u);
        assertSame(u, plan.getAssignee());
    }

    @Test
    public void assigneeCannotBeSet() {
        Plan plan = new Plan(new User(), "the plan");
        assertThrows(UnsupportedOperationException.class,
                     () -> plan.setAssignee(new User()));
    }

    private static List<PlanBucket> bucketOrder(Plan plan) {
        List<PlanBucket> buckets = plan.getBuckets()
                .stream()
                .sorted(PlanBucket.BY_POSITION)
                .toList();
        for (int i = 1; i < buckets.size(); i++) {
            assertTrue(buckets.get(i - 1).getPosition() < buckets.get(i).getPosition(),
                       "positions are unique");
        }
        return buckets;
    }

    @Test
    void moveBucket_first() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        plan.moveBucket(c, null);

        assertEquals(List.of(c, a, b), bucketOrder(plan));
    }

    @Test
    void moveBucket_afterEarlier() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        plan.moveBucket(c, a);

        assertEquals(List.of(a, c, b), bucketOrder(plan));
    }

    @Test
    void moveBucket_afterLater() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        plan.moveBucket(a, b);

        assertEquals(List.of(b, a, c), bucketOrder(plan));
    }

    @Test
    void moveBucket_last() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        plan.moveBucket(a, c);

        assertEquals(List.of(b, c, a), bucketOrder(plan));
    }

    @Test
    void moveBucket_afterSelf() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);

        plan.moveBucket(b, b);

        assertEquals(List.of(a, b, c), bucketOrder(plan));
    }

    @Test
    void moveBucket_resolvesDuplicates() {
        Plan plan = new Plan("plan");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket b = new PlanBucket(plan, "b", null);
        PlanBucket c = new PlanBucket(plan, "c", null);
        b.setPosition(a.getPosition());

        plan.moveBucket(c, null);

        List<PlanBucket> order = bucketOrder(plan);
        assertEquals(c, order.get(0));
    }

    @Test
    void moveBucket_foreign() {
        Plan plan = new Plan("plan");
        Plan other = new Plan("other");
        PlanBucket a = new PlanBucket(plan, "a", null);
        PlanBucket x = new PlanBucket(other, "x", null);

        assertThrows(IllegalArgumentException.class,
                     () -> plan.moveBucket(x, null));
        assertThrows(IllegalArgumentException.class,
                     () -> plan.moveBucket(a, x));
    }

}
