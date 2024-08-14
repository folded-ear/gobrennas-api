package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlanServiceMockedTest extends MockTest {

    @MockTestTarget
    private PlanService service;

    @Test
    void getTreeDeltasById_plan() {
        var cutoff = Instant.now();
        var groceries = mock(Plan.class);
        when(groceries.getUpdatedAt())
                .thenReturn(cutoff.plusSeconds(1));
        var milk = mock(PlanItem.class);
        when(milk.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        var tues = mock(PlanBucket.class);
        when(tues.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        doReturn(groceries)
                .when(service)
                .getPlanById(any(), any());
        doReturn(List.of(groceries, milk))
                .when(service)
                .getTreeById(groceries);

        var result = service.getTreeDeltasById(groceries.getId(), cutoff);

        assertEquals(List.of(groceries), result);
    }

    @Test
    void getTreeDeltasById_item() {
        var cutoff = Instant.now();
        var groceries = mock(Plan.class);
        when(groceries.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        var milk = mock(PlanItem.class);
        when(milk.getUpdatedAt())
                .thenReturn(cutoff.plusSeconds(1));
        var tues = mock(PlanBucket.class);
        when(tues.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        doReturn(groceries)
                .when(service)
                .getPlanById(any(), any());
        doReturn(List.of(groceries, milk))
                .when(service)
                .getTreeById(groceries);

        var result = service.getTreeDeltasById(groceries.getId(), cutoff);

        assertEquals(List.of(milk), result);
    }

    @Test
    void getTreeDeltasById_deletedItem() {
        var cutoff = Instant.now();
        var groceries = mock(Plan.class);
        when(groceries.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        var milk = mock(PlanItem.class);
        when(milk.getUpdatedAt())
                .thenReturn(cutoff.plusSeconds(1));
        var tues = mock(PlanBucket.class);
        when(tues.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        doReturn(groceries)
                .when(service)
                .getPlanById(any(), any());
        doReturn(List.of(groceries))
                .when(service)
                .getTreeById(groceries);
        when(groceries.getTrashBinItems())
                .thenReturn(Set.of(milk));

        var result = service.getTreeDeltasById(groceries.getId(), cutoff);

        assertEquals(List.of(milk), result);
    }

    @Test
    void getTreeDeltasById_bucket() {
        var cutoff = Instant.now();
        var groceries = mock(Plan.class);
        when(groceries.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        var milk = mock(PlanItem.class);
        when(milk.getUpdatedAt())
                .thenReturn(cutoff.minusSeconds(1));
        var tues = mock(PlanBucket.class);
        when(tues.getUpdatedAt())
                .thenReturn(cutoff.plusSeconds(1));
        doReturn(groceries)
                .when(service)
                .getPlanById(any(), any());
        doReturn(List.of(groceries, milk))
                .when(service)
                .getTreeById(groceries);
        when(groceries.getBuckets())
                .thenReturn(Set.of(tues));

        var result = service.getTreeDeltasById(groceries.getId(), cutoff);

        assertEquals(List.of(groceries), result);
    }

}
