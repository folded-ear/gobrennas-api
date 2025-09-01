package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;

@Controller
public class PlannerQuery {

    @Autowired
    private PlanService planService;

    @SchemaMapping(typeName = "PlannerQuery")
    Iterable<Plan> plans(DataFetchingEnvironment env) {
        return planService.getPlans(PrincipalUtil.from(env).getId());
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
