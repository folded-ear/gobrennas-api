package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;

@Controller
public class PlannerQueryController {

    record PlannerQuery() {}

    @Autowired
    private PlanService planService;

    @QueryMapping
    PlannerQuery planner() {
        return new PlannerQuery();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Iterable<Plan> plans(PlannerQuery planQ,
                         @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return planService.getPlans(userPrincipal.getId());
    }

    @SchemaMapping
    Plan plan(PlannerQuery planQ,
              @Argument Long id) {
        return planService.getPlanById(id);
    }

    @SchemaMapping
    PlanItem planItem(PlannerQuery planQ,
                      @Argument Long id) {
        return planService.getPlanItemById(id);
    }

    @SchemaMapping
    CorePlanItem planOrItem(PlannerQuery planQ,
                            @Argument Long id) {
        return planService.getPlanItemById(id);
    }

    @SchemaMapping
    List<? extends CorePlanItem> updatedSince(PlannerQuery planQ,
                                              @Argument Long planId,
                                              @Argument Long cutoff) {
        return planService.getTreeDeltasById(planId,
                                             Instant.ofEpochMilli(cutoff));
    }

}
