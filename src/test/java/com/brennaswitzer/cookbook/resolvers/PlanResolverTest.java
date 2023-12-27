package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.MockTest;
import com.brennaswitzer.cookbook.util.MockTestTarget;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlanResolverTest extends MockTest {

    @MockTestTarget
    private PlanResolver resolver;

    @Mock
    private PlanService planService;

    @Test
    void grants() {
        User johann = mock(User.class);
        Acl acl = mock(Acl.class);
        when(acl.getGrants()).thenReturn(singletonMap(johann, AccessLevel.CHANGE));
        Plan plan = mock(Plan.class);
        when(plan.getAcl()).thenReturn(acl);

        List<AccessControlEntry> aces = resolver.grants(plan);

        assertEquals(1, aces.size());
        AccessControlEntry ace = aces.get(0);
        assertSame(johann, ace.user);
        assertEquals(AccessLevel.CHANGE, ace.level);
    }

    @Test
    void children() {
        Plan plan = mock(Plan.class);
        List<PlanItem> kids = new ArrayList<>();
        when(plan.getOrderedChildView()).thenReturn(kids);

        assertSame(kids, resolver.children(plan));
    }

    @Test
    void descendantCount() {
        Plan plan = mock(Plan.class);
        PlanItem kid = mock(PlanItem.class);
        when(planService.getTreeById(plan))
                .thenReturn(Arrays.asList(plan, kid));

        int dc = resolver.descendantCount(plan);

        assertEquals(1, dc);
        verify(planService).getTreeById(plan);
    }

    @Test
    void descendants() {
        Plan plan = mock(Plan.class);
        PlanItem kid = mock(PlanItem.class);
        when(planService.getTreeById(plan))
                .thenReturn(Arrays.asList(plan, kid));

        List<PlanItem> ds = resolver.descendants(plan);

        assertEquals(singletonList(kid), ds);
        verify(planService).getTreeById(plan);
    }

}