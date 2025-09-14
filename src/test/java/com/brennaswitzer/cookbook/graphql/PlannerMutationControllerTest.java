package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.services.PlanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannerMutationControllerTest {

    @InjectMocks
    private PlannerMutationController mutation;

    @Mock
    private PlanService planService;

    @Test
    void assignBucket() {
        long itemId = 123L;
        long bucketId = 456L;
        PlanItem mock = mock(PlanItem.class);
        when(planService.assignItemBucket(itemId, bucketId))
                .thenReturn(mock);

        PlanItem item = mutation.assignBucket(null, itemId, bucketId);

        assertSame(mock, item);
    }

    @Test
    void createBucket() {
        long planId = 123L;
        String name = "bucket";
        LocalDate date = LocalDate.of(2023, 12, 27);
        PlanBucket bucket = mock(PlanBucket.class);
        when(planService.createBucket(planId, name, date))
                .thenReturn(bucket);

        PlanBucket result = mutation.createBucket(null, planId, name, date);

        assertSame(bucket, result);
    }

    @Test
    void createItem() {
        long parentId = 123L;
        long afterId = 456L;
        String name = "cheese";
        PlanItem item = mock(PlanItem.class);
        when(planService.createItem(parentId, afterId, name))
                .thenReturn(item);

        PlanItem result = mutation.createItem(null, parentId, afterId, name);

        assertSame(item, result);
    }

    @Test
    void createPlan_blank() {
        String name = "cheese party";
        Plan plan = mock(Plan.class);
        when(planService.createPlan(name))
                .thenReturn(plan);

        Plan result = mutation.createPlan(null, name, null);

        assertSame(plan, result);
    }

    @Test
    void createPlan_from() {
        String name = "cheese party";
        long sourcePlanId = 456L;
        Plan plan = mock(Plan.class);
        when(planService.duplicatePlan(name, sourcePlanId))
                .thenReturn(plan);

        Plan result = mutation.createPlan(null, name, sourcePlanId);

        assertSame(plan, result);
    }

    @Test
    void deleteBucket() {
        long planId = 123L;
        long bucketId = 456L;
        PlanBucket bucket = mock(PlanBucket.class);
        when(bucket.getId()).thenReturn(bucketId);
        when(planService.deleteBucket(planId, bucketId))
                .thenReturn(bucket);

        var result = mutation.deleteBucket(null, planId, bucketId);

        assertEquals(Long.valueOf(456L), result.getId());
    }

    @Test
    void deleteItem() {
        long itemId = 123L;
        PlanItem item = mock(PlanItem.class);
        when(item.getId()).thenReturn(itemId);
        when(planService.deleteItem(itemId))
                .thenReturn(item);

        var result = mutation.deleteItem(null, itemId);

        assertEquals(Long.valueOf(itemId), result.getId());
    }

    @Test
    void deletePlan() {
        long planId = 123L;
        Plan plan = mock(Plan.class);
        when(plan.getId()).thenReturn(planId);
        when(planService.deletePlan(planId))
                .thenReturn(plan);

        var result = mutation.deletePlan(null, planId);

        assertEquals(Long.valueOf(planId), result.getId());
    }

    @Test
    void duplicatePlan() {
        String name = "cheese party";
        long sourcePlanId = 456L;
        Plan plan = mock(Plan.class);
        when(planService.duplicatePlan(name, sourcePlanId))
                .thenReturn(plan);

        Plan result = mutation.duplicatePlan(null, name, sourcePlanId);

        assertSame(plan, result);
    }

    @Test
    void mutateTree() {
        List<Long> itemIds = Arrays.asList(456L, 789L);
        long parentId = 123L;
        long afterId = 999L;
        PlanItem parent = mock(PlanItem.class);
        when(planService.mutateTree(itemIds, parentId, afterId))
                .thenReturn(parent);

        PlanItem result = mutation.mutateTree(null, 
                new MutatePlanTree(itemIds, parentId, afterId));

        assertSame(parent, result);
    }

    @Test
    void rename() {
        long id = 123L;
        String newName = "goat log";
        PlanItem item = mock(PlanItem.class);
        when(planService.renameItem(id, newName))
                .thenReturn(item);

        var result = mutation.rename(null, id, newName);

        assertSame(item, result);
    }

    @Test
    void reorderSubitems() {
        long parentId = 123L;
        List<Long> itemIds = Arrays.asList(456L, 789L);
        PlanItem parent = mock(PlanItem.class);
        when(planService.resetSubitems(parentId, itemIds))
                .thenReturn(parent);

        PlanItem result = mutation.reorderSubitems(null, parentId, itemIds);

        assertSame(parent, result);
    }

    @Test
    void setGrant() {
        long planId = 123L;
        long userId = 456L;
        AccessLevel level = AccessLevel.CHANGE;
        Plan plan = mock(Plan.class);
        when(planService.setGrantOnPlan(planId, userId, level))
                .thenReturn(plan);

        Plan result = mutation.setGrant(null, planId, userId, level);

        assertSame(plan, result);
    }

    @Test
    void setStatus() {
        long id = 123L;
        PlanItemStatus status = PlanItemStatus.ACQUIRED;
        PlanItem item = mock(PlanItem.class);
        Instant date = Instant.now().minus(3, ChronoUnit.DAYS);
        when(planService.setItemStatus(id, status, date))
                .thenReturn(item);

        PlanItem result = mutation.setStatus(null, id, status, date);

        assertSame(item, result);
    }

    @Test
    void revokeGrant() {
        long planId = 123L;
        long userId = 456L;
        Plan plan = mock(Plan.class);
        when(planService.revokeGrantFromPlan(planId, userId))
                .thenReturn(plan);

        Plan result = mutation.revokeGrant(null, planId, userId);

        assertSame(plan, result);
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

        PlanBucket bucket = mutation.updateBucket(null, planId, bucketId, name, date);

        assertSame(mock, bucket);
    }
}
