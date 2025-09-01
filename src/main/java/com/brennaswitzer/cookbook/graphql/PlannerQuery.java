package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;

@Controller
public class PlannerQuery {

    @Autowired
    private PlanService planService;

    @SchemaMapping(typeName = "PlannerQuery")
    @PreAuthorize("hasRole('USER')")
    Iterable<Plan> plans(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return planService.getPlans(userPrincipal.getId());
    }

    @SchemaMapping(typeName = "PlannerQuery")
    Plan plan(@Argument Long id) {
        return planService.getPlanById(id);
    }

    @SchemaMapping(typeName = "PlannerQuery")
    PlanItem planItem(@Argument Long id) {
        return planService.getPlanItemById(id);
    }

    @SchemaMapping(typeName = "PlannerQuery")
    CorePlanItem planOrItem(@Argument Long id) {
        return planService.getPlanItemById(id);
    }

    @SchemaMapping(typeName = "PlannerQuery")
    List<? extends CorePlanItem> updatedSince(@Argument Long planId,
                                              @Argument Long cutoff) {
        return planService.getTreeDeltasById(planId,
                                             Instant.ofEpochMilli(cutoff));
    }

}
