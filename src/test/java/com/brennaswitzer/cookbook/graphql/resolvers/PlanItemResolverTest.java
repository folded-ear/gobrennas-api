package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanItemResolverTest extends MockTest {

    @MockTestTarget
    private PlanItemResolver resolver;

    @Mock
    private PlanService planService;

    @Test
    void parent_plan() {
        Plan plan = mock(Plan.class);
        when(plan.getParent()).thenReturn(null);
        when(plan.getPlan()).thenReturn(plan);

        PlanItem p = resolver.parent(plan);

        assertNull(p, "Plan don't have parents");
    }

    @Test
    void parent_item() {
        Plan plan = mock(Plan.class);
        PlanItem item = mock(PlanItem.class);
        when(item.getParent()).thenReturn(plan);
        when(item.getPlan()).thenReturn(plan);

        PlanItem p = resolver.parent(item);

        assertSame(plan, p);
    }

    @Test
    void children() {
        PlanItem item = mock(PlanItem.class);
        List<PlanItem> kids = new ArrayList<>();
        when(item.getOrderedChildView()).thenReturn(kids);

        assertSame(kids, resolver.children(item));
    }

    @Test
    void components() {
        PlanItem item = mock(PlanItem.class);
        List<PlanItem> comps = new ArrayList<>();
        when(item.getOrderedComponentsView()).thenReturn(comps);

        assertSame(comps, resolver.components(item));
    }

    @Test
    void descendantCount() {
        PlanItem item = mock(PlanItem.class);
        PlanItem kid = mock(PlanItem.class);
        when(planService.getTreeById(item))
                .thenReturn(Arrays.asList(item, kid));

        int dc = resolver.descendantCount(item);

        assertEquals(1, dc);
        verify(planService).getTreeById(item);
    }

    @Test
    void descendants() {
        PlanItem item = mock(PlanItem.class);
        PlanItem kid = mock(PlanItem.class);
        when(planService.getTreeById(item))
                .thenReturn(Arrays.asList(item, kid));

        List<PlanItem> ds = resolver.descendants(item);

        assertEquals(singletonList(kid), ds);
        verify(planService).getTreeById(item);
    }

}
