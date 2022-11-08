package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.message.*;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.services.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@MessageMapping("/plan") // todo: cull
@PreAuthorize("hasRole('USER')")
public class PlanController {

    @Autowired
    private PlanService planService;

    @SubscribeMapping("/{id}") // todo: cull
    public List<TaskInfo> subscribeToList(@DestinationVariable("id") Long id) {
        // Packaging up the entire tree into a single message is sorta less than
        // ideal. Sending only the top-level items w/ a follow-up message
        // containing the rest would give better perceived performance, as the
        // visible tasks would show up w/out having to wait for processing all
        // the nested subtasks, which are likely _much_ more numerous.
        //
        // I think?
        return TaskInfo.fromTasks(planService.getTreeById(id));
    }

    @MessageMapping("/{id}/mutate-tree") // todo: cull
    public void mutateTree(@Payload MutatePlanTree action) {
        planService.mutateTree(action.getIds(), action.getParentId(), action.getAfterId());
    }

    @MessageMapping("/{id}/reorder-items") // todo: cull
    public void reorderSubitems(@Payload ReorderSubitems action) {
        planService.resetSubitems(action.getId(), action.getSubitemIds());
    }

    @MessageMapping("/{id}/create") // todo: cull
    public void createItem(@Payload CreatePlanTreeItem action) {
        planService.createItem(action.getId(), action.getParentId(), action.getAfterId(), action.getName());
    }

    @MessageMapping("/{id}/rename") // todo: cull
    public void renameItem(@Payload RenamePlanTreeItem action) {
        planService.renameItem(action.getId(), action.getName());
    }

    @MessageMapping("/{id}/assign-bucket") // todo: cull
    public void assignItemBucket(@Payload AssignPlanTreeItemBucket action) {
        planService.assignItemBucket(action.getId(), action.getBucketId());
    }

    @MessageMapping("/{id}/status") // todo: cull
    public void setStatus(@Payload SetPlanTreeItemStatus action) {
        planService.setItemStatus(action.getId(), action.getStatus());
    }

    @MessageMapping("/{id}/delete") // todo: cull
    public void deleteItem(@Payload DeletePlanTreeItem action) {
        planService.deleteItem(action.getId());
    }

    @MessageMapping("/{id}/buckets/create") // todo: cull
    public void createBucket(@DestinationVariable("id") long planId, @Payload CreatePlanBucket action) {
        planService.createBucket(planId, action.getId(), action.getName(), action.getDate());
    }

    @MessageMapping("/{id}/buckets/update") // todo: cull
    public void updateBucket(@DestinationVariable("id") long planId, @Payload UpdatePlanBucket action) {
        planService.updateBucket(planId, action.getId(), action.getName(), action.getDate());
    }

    @MessageMapping("/{id}/buckets/delete") // todo: cull
    public void deleteBucket(@DestinationVariable("id") long planId, @Payload DeletePlanBucket action) {
        planService.deleteBucket(planId, action.getId());
    }

}
