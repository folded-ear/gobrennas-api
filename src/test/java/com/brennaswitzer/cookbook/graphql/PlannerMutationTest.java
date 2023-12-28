package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlannerMutationTest extends MockTest {

    @MockTestTarget
    private PlannerMutation mutation;

    @Mock
    private PlanService planService;

    @Test
    void assignBucket() {
        long itemId = 123L;
        long bucketId = 456L;
        PlanItem mock = mock(PlanItem.class);
        when(planService.assignItemBucket(itemId, bucketId))
                .thenReturn(mock);

        PlanItem item = mutation.assignBucket(itemId, bucketId);

        assertSame(mock, item);
    }

    @Test
    void createBucket() {
        long planId = 123L;
        String name = "bucket";
        LocalDate date = LocalDate.of(2023, 12, 27);
        PlanBucket mock = mock(PlanBucket.class);
        when(planService.createBucket(planId, name, date))
                .thenReturn(mock);

        PlanBucket bucket = mutation.createBucket(planId, name, date);

        assertSame(mock, bucket);
    }

    @Test
    void deleteBucket() {
        long planId = 123L;
        long bucketId = 456L;
        Plan plan = mock(Plan.class);
        PlanBucket bucket = mock(PlanBucket.class);
        when(bucket.getPlan()).thenReturn(plan);
        when(planService.deleteBucket(planId, bucketId))
                .thenReturn(bucket);

        Plan result = mutation.deleteBucket(planId, bucketId);

        assertSame(plan, result);
    }

    @Test
    void rename() {
        long id = 123L;
        String newName = "goat log";
        PlanItem mock = mock(PlanItem.class);
        when(planService.renameItem(id, newName))
                .thenReturn(mock);

        PlanItem item = mutation.rename(id, newName);

        assertSame(mock, item);
    }

    @Test
    void updateBucket() {
        long planId = 123L;
        long bucketId = 456L;
        String name = "bucket";
        LocalDate date = LocalDate.of(2023, 12, 28);
        PlanBucket mock = mock(PlanBucket.class);
        when(planService.updateBucket(planId, bucketId, name, date))
                .thenReturn(mock);

        PlanBucket bucket = mutation.updateBucket(planId, bucketId, name, date);

        assertSame(mock, bucket);
    }

}
