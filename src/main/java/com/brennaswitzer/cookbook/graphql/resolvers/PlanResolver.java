package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.graphql.model.AccessControlEntry;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;

import static com.brennaswitzer.cookbook.util.CollectionUtils.tail;

@Controller
public class PlanResolver {

    @Autowired
    private PlanService planService;

    @Autowired
    private ShareHelper shareHelper;

    @SchemaMapping
    public List<AccessControlEntry> grants(Plan plan) {
        return AclHelpers.getGrants(plan);
    }

    @SchemaMapping
    public List<PlanItem> children(Plan plan) {
        return plan.getOrderedChildView();
    }

    @SchemaMapping
    public int descendantCount(Plan plan) {
        return planService.getTreeById(plan).size() - 1;
    }

    @SchemaMapping
    public List<PlanItem> descendants(Plan plan) {
        return tail(planService.getTreeById(plan));
    }

    @SchemaMapping
    public ShareInfo share(Plan plan) {
        return shareHelper.getInfo(Plan.class, plan);
    }

    @SchemaMapping
    List<? extends CorePlanItem> updatedSince(Plan plan,
                                              @Argument Long cutoff) {
        return planService.getTreeDeltasById(plan.getId(),
                                             Instant.ofEpochMilli(cutoff));
    }

}
