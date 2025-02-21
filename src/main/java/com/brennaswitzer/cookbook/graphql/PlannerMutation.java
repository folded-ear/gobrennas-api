package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.CorePlanItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.graphql.model.Deletion;
import com.brennaswitzer.cookbook.graphql.model.UnsavedBucket;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
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

    public List<PlanBucket> createBuckets(Long planId, List<UnsavedBucket> buckets) {
        if (buckets.isEmpty()) return List.of();
        return planService.createBuckets(planId, buckets);
    }

    public PlanItem createItem(Long parentId, Long afterId, String name) {
        return planService.createItem(parentId, afterId, name);
    }

    public Plan createPlan(String name, Long sourcePlanId) {
        return sourcePlanId == null
                ? planService.createPlan(name)
                : duplicatePlan(name, sourcePlanId);
    }

    public Deletion deleteBucket(Long planId, Long bucketId) {
        return Deletion.of(planService.deleteBucket(planId, bucketId));
    }

    public List<Deletion> deleteBuckets(Long planId, List<Long> bucketIds) {
        if (bucketIds.isEmpty()) return List.of();
        return planService.deleteBuckets(planId, bucketIds)
                .stream()
                .map(Deletion::of)
                .toList();
    }

    public Deletion deleteItem(Long id) {
        return Deletion.of(planService.deleteItem(id));
    }

    public Deletion deletePlan(Long id) {
        return Deletion.of(planService.deletePlan(id));
    }

    public Plan duplicatePlan(String name, Long sourcePlanId) {
        return planService.duplicatePlan(name, sourcePlanId);
    }

    public PlanItem mutateTree(MutatePlanTree spec) {
        return planService.mutateTree(spec.getIds(),
                                      spec.getParentId(),
                                      spec.getAfterId());
    }

    public CorePlanItem rename(Long id, String name) {
        return planService.renameItem(id, name);
    }

    public PlanItem reorderSubitems(Long parentId, List<Long> itemIds) {
        return planService.resetSubitems(parentId, itemIds);
    }

    public PlanBucket updateBucket(Long planId, Long bucketId, String name, LocalDate date) {
        return planService.updateBucket(planId, bucketId, name, date);
    }

    public Plan setColor(Long planId, String color) {
        return planService.setColor(planId, color);
    }

    public Plan setGrant(Long planId, Long userId, AccessLevel accessLevel) {
        return planService.setGrantOnPlan(planId, userId, accessLevel);
    }

    public Plan updatePlanNotes(Long planId, String notes) {
        return planService.updatePlanNotes(planId, notes);
    }

    public PlanItem setStatus(Long id, PlanItemStatus status, Instant doneAt) {
        return planService.setItemStatus(id, status, doneAt);
    }

    public Plan revokeGrant(Long planId, Long userId) {
        return planService.revokeGrantFromPlan(planId, userId);
    }

}
