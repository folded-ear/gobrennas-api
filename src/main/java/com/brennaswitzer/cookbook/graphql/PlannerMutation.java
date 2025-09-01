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
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Controller
public class PlannerMutation {

    @Autowired
    private PlanService planService;

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanItem assignBucket(@Argument Long id,
                                 @Argument Long bucketId) {
        return planService.assignItemBucket(id, bucketId);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanBucket createBucket(@Argument Long planId,
                                   @Argument String name,
                                   @Argument LocalDate date) {
        return planService.createBucket(planId, name, date);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public List<PlanBucket> createBuckets(@Argument Long planId,
                                          @Argument List<UnsavedBucket> buckets) {
        if (buckets.isEmpty()) return List.of();
        return planService.createBuckets(planId, buckets);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanItem createItem(@Argument Long parentId,
                               @Argument Long afterId,
                               @Argument String name) {
        return planService.createItem(parentId, afterId, name);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan createPlan(@Argument String name,
                           @Argument Long sourcePlanId) {
        return sourcePlanId == null
                ? planService.createPlan(name)
                : duplicatePlan(name, sourcePlanId);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Deletion deleteBucket(@Argument Long planId,
                                 @Argument Long bucketId) {
        return Deletion.of(planService.deleteBucket(planId, bucketId));
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public List<Deletion> deleteBuckets(@Argument Long planId,
                                        @Argument List<Long> bucketIds) {
        if (bucketIds.isEmpty()) return List.of();
        return planService.deleteBuckets(planId, bucketIds)
                .stream()
                .map(Deletion::of)
                .toList();
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Deletion deleteItem(@Argument Long id) {
        return Deletion.of(planService.deleteItem(id));
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Deletion deletePlan(@Argument Long id) {
        return Deletion.of(planService.deletePlan(id));
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan duplicatePlan(@Argument String name,
                              @Argument Long sourcePlanId) {
        return planService.duplicatePlan(name, sourcePlanId);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanItem mutateTree(@Argument MutatePlanTree spec) {
        return planService.mutateTree(spec.getIds(),
                                      spec.getParentId(),
                                      spec.getAfterId());
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public CorePlanItem rename(@Argument Long id,
                               @Argument String name) {
        return planService.renameItem(id, name);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanItem reorderSubitems(@Argument Long parentId,
                                    @Argument List<Long> itemIds) {
        return planService.resetSubitems(parentId, itemIds);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanBucket updateBucket(@Argument Long planId,
                                   @Argument Long bucketId,
                                   @Argument String name,
                                   @Argument LocalDate date) {
        return planService.updateBucket(planId, bucketId, name, date);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan setColor(@Argument Long planId,
                         @Argument String color) {
        return planService.setColor(planId, color);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan setGrant(@Argument Long planId,
                         @Argument Long userId,
                         @Argument AccessLevel accessLevel) {
        return planService.setGrantOnPlan(planId, userId, accessLevel);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan updatePlanNotes(@Argument Long planId,
                                @Argument String notes) {
        return planService.updatePlanNotes(planId, notes);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public PlanItem setStatus(@Argument Long id,
                              @Argument PlanItemStatus status,
                              @Argument Instant doneAt) {
        return planService.setItemStatus(id, status, doneAt);
    }

    @SchemaMapping(typeName = "PlannerMutation")
    public Plan revokeGrant(@Argument Long planId,
                            @Argument Long userId) {
        return planService.revokeGrantFromPlan(planId, userId);
    }

}
