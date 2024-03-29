package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

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

    public PlanItem createItem(Long parentId, Long afterId, String name) {
        return planService.createItem(parentId, afterId, name);
    }

    public Plan createPlan(String name) {
        return planService.createPlan(name);
    }

    public Plan deleteBucket(Long planId, Long bucketId) {
        return planService.deleteBucket(planId, bucketId).getPlan();
    }

    public PlanItem deleteItem(Long id) {
        return planService.deleteItemForParent(id);
    }

    public boolean deletePlan(Long id) {
        planService.deletePlan(id);
        return true;
    }

    public Plan duplicatePlan(String name, Long sourcePlanId) {
        return planService.duplicatePlan(name, sourcePlanId);
    }

    public PlanItem mutateTree(List<Long> itemIds, Long parentId, Long afterId) {
        return planService.mutateTree(itemIds, parentId, afterId);
    }

    public PlanItem rename(Long id, String name) {
        return planService.renameItem(id, name);
    }

    public PlanItem reorderSubitems(Long parentId, List<Long> itemIds) {
        return planService.resetSubitems(parentId, itemIds);
    }

    public PlanBucket updateBucket(Long planId, Long bucketId, String name, LocalDate date) {
        return planService.updateBucket(planId, bucketId, name, date);
    }

    public Plan setGrant(Long planId, Long userId, AccessLevel accessLevel) {
        return planService.setGrantOnPlan(planId, userId, accessLevel);
    }

    public PlanItem setStatus(Long id, PlanItemStatus status) {
        return planService.setItemStatus(id, status);
    }

    public Plan deleteGrant(Long planId, Long userId) {
        return planService.deleteGrantFromPlan(planId, userId);
    }

}
