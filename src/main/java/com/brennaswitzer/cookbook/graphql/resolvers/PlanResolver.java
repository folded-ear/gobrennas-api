package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static com.brennaswitzer.cookbook.util.CollectionUtils.tail;

@Component
public class PlanResolver implements GraphQLResolver<Plan> {

    @Autowired
    private PlanService planService;

    @Autowired
    private ShareHelper shareHelper;

    public List<AccessControlEntry> grants(Plan plan) {
        return AclHelpers.getGrants(plan);
    }

    public List<PlanItem> children(Plan plan) {
        return plan.getOrderedChildView();
    }

    public int descendantCount(Plan plan) {
        return planService.getTreeById(plan).size() - 1;
    }

    public List<PlanItem> descendants(Plan plan) {
        return tail(planService.getTreeById(plan));
    }

    public ShareInfo share(Plan plan) {
        return shareHelper.getInfo(Plan.class, plan);
    }

    List<? extends CorePlanItem> updatedSince(Plan plan, Long cutoff) {
        return planService.getTreeDeltasById(plan.getId(),
                                             Instant.ofEpochMilli(cutoff));
    }

}
