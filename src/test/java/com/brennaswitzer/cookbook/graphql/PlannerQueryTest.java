package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlannerQueryTest extends MockTest {

    @MockTestTarget
    private PlannerQuery query;

    @Mock
    private PlanService planService;

    @Test
    void plans() {
        Plan a = mock(Plan.class);
        when(a.getName()).thenReturn("A");
        Plan b = mock(Plan.class);
        when(b.getName()).thenReturn("B");
        when(planService.getPlans())
                .thenReturn(Arrays.asList(a, b));

        var ps = query.plans();

        assertEquals(2, ps.size());
        assertEach(Arrays.asList("A", "B"), ps, Plan::getName);
        verify(planService).getPlans();
    }

    @Test
    void planItem() {
        PlanItem it = mock(PlanItem.class);
        when(planService.getPlanItemById(any())).thenReturn(it);

        var pi = query.planItem(123L);

        assertSame(pi, it);
        verify(planService).getPlanItemById(123L);
    }

    @Test
    void updatedSince() {
        List<PlanItem> list = new ArrayList<>();
        when(planService.getTreeDeltasById(123L,
                                           Instant.EPOCH.plusMillis(456)))
                .thenReturn(list);

        List<PlanItem> result = query.updatedSince(123L, 456L);

        assertSame(list, result);
    }

    private <O, V> void assertEach(List<V> expected, List<O> objects, Function<O, V> extractor) {
        assertEquals(expected.size(), objects.size());
        var ei = expected.iterator();
        var oi = objects.iterator();
        for (int i = 0; ei.hasNext(); i++) {
            assertEquals(ei.next(),
                         extractor.apply(oi.next()),
                         String.format("Values didn't match at %s", i));
        }
    }

}
