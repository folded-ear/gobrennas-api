package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.message.*;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/plan")
@PreAuthorize("hasRole('USER')")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping("/{id}/descendants")
    public List<TaskInfo> getDescendants(
            @PathVariable("id") Long id
    ) {
        return TaskInfo.fromTasks(planService
                .getTreeById(id));
    }

    @GetMapping("/{id}/all-since")
    public List<TaskInfo> getUpdatedSince(
            @PathVariable("id") Long id,
            @RequestParam Long cutoff
    ) {
        return TaskInfo.fromTasks(planService
                .getTreeDeltasById(id, Instant.ofEpochMilli(cutoff)));
    }

    @PostMapping("/{id}/mutate-tree")
    public PlanMessage mutateTree(
            @PathVariable("id") Long id,
            @RequestBody MutatePlanTree action
    ) {
        return planService.mutateTree(action.getIds(), action.getParentId(), action.getAfterId());
    }

    @PostMapping("/{id}/reorder-subitems")
    public PlanMessage reorderSubitems(
            @PathVariable("id") Long id,
            @RequestBody ReorderSubitems action) {
        return planService.resetSubitems(action.getId(), action.getSubitemIds());
    }

    @PostMapping("/{id}")
    public PlanMessage createItem(
            @PathVariable("id") Long id,
            @RequestBody CreatePlanTreeItem action
    ) {
        return planService.createItem(action.getId(), action.getParentId(), action.getAfterId(), action.getName());
    }

    @PutMapping("/{id}/rename")
    public PlanMessage renameItem(
            @PathVariable("id") Long id,
            @RequestBody RenamePlanTreeItem action
    ) {
        return planService.renameItem(action.getId(), action.getName());
    }

    @PostMapping("/{id}/assign-bucket")
    public PlanMessage assignItemBucket(
            @PathVariable("id") Long id,
            @RequestBody AssignPlanTreeItemBucket action) {
        return planService.assignItemBucket(action.getId(), action.getBucketId());
    }

    @PutMapping("/{id}/status")
    public PlanMessage setStatus(
            @PathVariable("id") Long id,
            @RequestBody SetPlanTreeItemStatus action
    ) {
        return planService.setItemStatus(action.getId(), action.getStatus());
    }

    @DeleteMapping("/{planId}/{id}")
    public void deleteItem(
            @PathVariable("planId") Long planId,
            @PathVariable("id") Long id) {
        planService.deleteItem(id);
    }

    @PostMapping("/{id}/buckets")
    public PlanMessage createBucket(
            @PathVariable("id") long planId,
            @RequestBody CreatePlanBucket action) {
        return planService.createBucket(planId, action.getId(), action.getName(), action.getDate());
    }

    @PutMapping("/{planId}/buckets/{id}")
    public PlanMessage updateBucket(
            @PathVariable("planId") long planId,
            @PathVariable("id") long id,
            @RequestBody UpdatePlanBucket action) {
        return planService.updateBucket(planId, action.getId(), action.getName(), action.getDate());
    }

    @DeleteMapping("/{planId}/buckets/{id}")
    public PlanMessage deleteBucket(
            @PathVariable("planId") long planId,
            @PathVariable("id") long id) {
        return planService.deleteBucket(planId, id);
    }

}
