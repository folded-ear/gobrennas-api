package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class PlannerQuery {

    @Autowired
    private PlanService planService;

    List<Plan> plans() {
        List<Plan> result = new ArrayList<>();
        Iterable<Plan> plans = planService.getPlans();
        Iterator<Plan> iterator = plans.iterator();
        iterator.forEachRemaining(result::add);
        return result;
    }

    PlanItem planItem(Long id) {
        return planService.getPlanItemById(id);
    }

    List<PlanItem> updatedSince(Long planId, Long cutoff) {
        return planService.getTreeDeltasById(planId,
                                             Instant.ofEpochMilli(cutoff));
    }

}
