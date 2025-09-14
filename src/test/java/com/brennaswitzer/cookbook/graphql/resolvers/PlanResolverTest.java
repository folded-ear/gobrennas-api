package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.model.AccessControlEntry;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class PlanResolverTest {

    @InjectMocks
    private PlanResolver resolver;

    @Mock
    private PlanService planService;

    @Mock
    private ShareHelper shareHelper;

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
        assertSame(johann, ace.getUser());
        assertEquals(AccessLevel.CHANGE, ace.getLevel());
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

    @Test
    void share() {
        Plan plan = mock(Plan.class);
        ShareInfo info = mock(ShareInfo.class);
        when(shareHelper.getInfo(Plan.class, plan))
                .thenReturn(info);

        ShareInfo result = resolver.share(plan);

        assertSame(info, result);
    }

}
