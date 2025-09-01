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
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Controller
public class PlannerMutationController {

    record PlannerMutation() {}

    @Autowired
    private PlanService planService;

    @MutationMapping
    PlannerMutation planner() {
        return new PlannerMutation();
    }

    @SchemaMapping
    PlanItem assignBucket(PlannerMutation planMut,
                          @Argument Long id,
                          @Argument Long bucketId) {
        return planService.assignItemBucket(id, bucketId);
    }

    @SchemaMapping
    PlanBucket createBucket(PlannerMutation planMut,
                            @Argument Long planId,
                            @Argument String name,
                            @Argument LocalDate date) {
        return planService.createBucket(planId, name, date);
    }

    @SchemaMapping
    List<PlanBucket> createBuckets(PlannerMutation planMut,
                                   @Argument Long planId,
                                   @Argument List<UnsavedBucket> buckets) {
        if (buckets.isEmpty()) return List.of();
        return planService.createBuckets(planId, buckets);
    }

    @SchemaMapping
    PlanItem createItem(PlannerMutation planMut,
                        @Argument Long parentId,
                        @Argument Long afterId,
                        @Argument String name) {
        return planService.createItem(parentId, afterId, name);
    }

    @SchemaMapping
    Plan createPlan(PlannerMutation planMut,
                    @Argument String name,
                    @Argument Long sourcePlanId) {
        return sourcePlanId == null
                ? planService.createPlan(name)
                : duplicatePlan(planMut, name, sourcePlanId);
    }

    @SchemaMapping
    Deletion deleteBucket(PlannerMutation planMut,
                          @Argument Long planId,
                          @Argument Long bucketId) {
        return Deletion.of(planService.deleteBucket(planId, bucketId));
    }

    @SchemaMapping
    List<Deletion> deleteBuckets(PlannerMutation planMut,
                                 @Argument Long planId,
                                 @Argument List<Long> bucketIds) {
        if (bucketIds.isEmpty()) return List.of();
        return planService.deleteBuckets(planId, bucketIds)
                .stream()
                .map(Deletion::of)
                .toList();
    }

    @SchemaMapping
    Deletion deleteItem(PlannerMutation planMut,
                        @Argument Long id) {
        return Deletion.of(planService.deleteItem(id));
    }

    @SchemaMapping
    Deletion deletePlan(PlannerMutation planMut,
                        @Argument Long id) {
        return Deletion.of(planService.deletePlan(id));
    }

    @SchemaMapping
    Plan duplicatePlan(PlannerMutation planMut,
                       @Argument String name,
                       @Argument Long sourcePlanId) {
        return planService.duplicatePlan(name, sourcePlanId);
    }

    @SchemaMapping
    PlanItem mutateTree(PlannerMutation planMut,
                        @Argument MutatePlanTree spec) {
        return planService.mutateTree(spec.getIds(),
                                      spec.getParentId(),
                                      spec.getAfterId());
    }

    @SchemaMapping
    CorePlanItem rename(PlannerMutation planMut,
                        @Argument Long id,
                        @Argument String name) {
        return planService.renameItem(id, name);
    }

    @SchemaMapping
    PlanItem reorderSubitems(PlannerMutation planMut,
                             @Argument Long parentId,
                             @Argument List<Long> itemIds) {
        return planService.resetSubitems(parentId, itemIds);
    }

    @SchemaMapping
    PlanBucket updateBucket(PlannerMutation planMut,
                            @Argument Long planId,
                            @Argument Long bucketId,
                            @Argument String name,
                            @Argument LocalDate date) {
        return planService.updateBucket(planId, bucketId, name, date);
    }

    @SchemaMapping
    Plan setColor(PlannerMutation planMut,
                  @Argument Long planId,
                  @Argument String color) {
        return planService.setColor(planId, color);
    }

    @SchemaMapping
    Plan setGrant(PlannerMutation planMut,
                  @Argument Long planId,
                  @Argument Long userId,
                  @Argument AccessLevel accessLevel) {
        return planService.setGrantOnPlan(planId, userId, accessLevel);
    }

    @SchemaMapping
    Plan updatePlanNotes(PlannerMutation planMut,
                         @Argument Long planId,
                         @Argument String notes) {
        return planService.updatePlanNotes(planId, notes);
    }

    @SchemaMapping
    PlanItem setStatus(PlannerMutation planMut,
                       @Argument Long id,
                       @Argument PlanItemStatus status,
                       @Argument Instant doneAt) {
        return planService.setItemStatus(id, status, doneAt);
    }

    @SchemaMapping
    Plan revokeGrant(PlannerMutation planMut,
                     @Argument Long planId,
                     @Argument Long userId) {
        return planService.revokeGrantFromPlan(planId, userId);
    }

}
