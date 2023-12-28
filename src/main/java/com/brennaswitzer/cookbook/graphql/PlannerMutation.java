package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class PlannerMutation {

    @Autowired
    private PlanService planService;

    public PlanItem assignBucket(Long id, Long bucketId) {
        return planService.assignItemBucket(id, bucketId);
    }

    public PlanBucket createBucket(Long planId, String name, LocalDate date) {
        return planService.createBucket(planId, name, date);
    }

    public Plan deleteBucket(Long planId, Long bucketId) {
        return planService.deleteBucket(planId, bucketId).getPlan();
    }

    public PlanItem rename(Long id, String name) {
        return planService.renameItem(id, name);
    }

    public PlanBucket updateBucket(Long planId, Long bucketId, String name, LocalDate date) {
        return planService.updateBucket(planId, bucketId, name, date);
    }

}
