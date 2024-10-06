package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlannerQuery {

    @Autowired
    private PlanService planService;

    List<Plan> plans(DataFetchingEnvironment env) {
        List<Plan> result = new ArrayList<>();
        planService.getPlans(PrincipalUtil.from(env).getId())
                .forEach(result::add);
        return result;
    }

    Plan plan(Long id) {
        return planService.getPlanById(id);
    }

    PlanItem planItem(Long id) {
        return planService.getPlanItemById(id);
    }

    List<? extends CorePlanItem> updatedSince(Long planId, Long cutoff) {
        return planService.getTreeDeltasById(planId,
                                             Instant.ofEpochMilli(cutoff));
    }

}
