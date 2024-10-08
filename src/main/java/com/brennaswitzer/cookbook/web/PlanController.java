package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.message.AssignPlanTreeItemBucket;
import com.brennaswitzer.cookbook.message.CreatePlanBucket;
import com.brennaswitzer.cookbook.message.CreatePlanTreeItem;
import com.brennaswitzer.cookbook.message.MutatePlanTree;
import com.brennaswitzer.cookbook.message.PlanMessage;
import com.brennaswitzer.cookbook.message.RenamePlanTreeItem;
import com.brennaswitzer.cookbook.message.ReorderSubitems;
import com.brennaswitzer.cookbook.message.SetPlanTreeItemStatus;
import com.brennaswitzer.cookbook.message.UpdatePlanBucket;
import com.brennaswitzer.cookbook.payload.AclInfo;
import com.brennaswitzer.cookbook.payload.GrantInfo;
import com.brennaswitzer.cookbook.payload.PlanItemCreate;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping({ "api/plan",
        "api/tasks" })
@PreAuthorize("hasRole('USER')")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private ShareHelper shareHelper;

    @Autowired
    private UserRepository userRepo;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanItemInfo> getPlans() {
        return PlanItemInfo.from(planService.getPlans());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlanItemInfo createPlan(@RequestBody PlanItemCreate info) {
        Plan plan = info.hasFromId()
                ? planService.duplicatePlan(info.getName(),
                                            info.getFromId())
                : planService.createPlan(info.getName());
        return PlanItemInfo.from(plan);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanItemInfo getPlanItem(
            @PathVariable("id") Long id
    ) {
        return PlanItemInfo.from(planService.getPlanItemById(id));
    }

    @GetMapping("/{id}/acl")
    @ResponseStatus(HttpStatus.OK)
    public AclInfo getPlanAcl(
            @PathVariable("id") Long id
    ) {
        Plan plan = planService.getPlanById(id);
        return AclInfo.fromAcl(plan.getAcl());
    }

    @GetMapping("/{id}/share")
    public ShareInfo getShareInfoById(
            @PathVariable("id") Long id
    ) {
        return shareHelper.getInfo(Plan.class,
                                   planService.getPlanById(id));
    }

    @GetMapping({ "/{id}/self-and-descendants",
            "/{id}/descendants" })
    public List<PlanItemInfo> getDescendants(
            @PathVariable("id") Long id
    ) {
        return PlanItemInfo.from(
                planService.getTreeById(id));
    }

    @GetMapping("/{id}/all-since")
    public List<PlanItemInfo> getUpdatedSince(
            @PathVariable("id") Long id,
            @RequestParam Long cutoff
    ) {
        return PlanItemInfo.from(
                planService.getTreeDeltasById(
                        id,
                        Instant.ofEpochMilli(cutoff)));
    }

    @PostMapping("/{id}/mutate-tree")
    public PlanMessage mutateTree(
            @PathVariable("id") Long id,
            @RequestBody MutatePlanTree action
    ) {
        assert Objects.equals(id, action.getParentId())
                : String.format("ID mismatch on rename (%s on URL, %s in action)",
                                id,
                                action.getParentId());
        return planService.mutateTreeForMessage(action.getIds(), action.getParentId(), action.getAfterId());
    }

    @PostMapping("/{id}/reorder-subitems")
    public PlanMessage reorderSubitems(
            @PathVariable("id") Long id,
            @RequestBody ReorderSubitems action) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on reorder (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.resetSubitemsForMessage(action.getId(), action.getSubitemIds());
    }

    @PostMapping("/{id}")
    public PlanMessage createItem(
            @PathVariable("id") Long id,
            @RequestBody CreatePlanTreeItem action
    ) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on create (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.createItemForMessage(action.getId(),
                                                action.getParentId(),
                                                action.getAfterId(),
                                                action.getName());
    }

    @PutMapping("/{id}/rename")
    public PlanMessage renameItem(
            @PathVariable("id") Long id,
            @RequestBody RenamePlanTreeItem action
    ) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on rename (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.renameItemForMessage(action.getId(), action.getName());
    }

    @PostMapping("/{id}/assign-bucket")
    public PlanMessage assignItemBucket(
            @PathVariable("id") Long id,
            @RequestBody AssignPlanTreeItemBucket action) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on assign bucket (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.assignItemBucketForMessage(action.getId(), action.getBucketId());
    }

    @PutMapping("/{id}/status")
    public PlanMessage setStatus(
            @PathVariable("id") Long id,
            @RequestBody SetPlanTreeItemStatus action
    ) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on set status (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.setItemStatusForMessage(action.getId(), action.getStatus());
    }

    @DeleteMapping("/{planId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlan(
            @PathVariable("planId") Long planId
    ) {
        planService.deletePlan(planId);
    }

    @DeleteMapping("/{planId}/{id}")
    public void deleteItem(
            @PathVariable("planId") Long planId,
            @PathVariable("id") Long id) {
        PlanItem item = planService.getPlanItemById(id);
        if (!planId.equals(item.getPlan().getId())) {
            throw new IllegalArgumentException("Item belongs to a different plan");
        }
        planService.deleteItem(id);
    }

    @PostMapping("/{id}/buckets")
    public PlanMessage createBucket(
            @PathVariable("id") long planId,
            @RequestBody CreatePlanBucket action) {
        return planService.createBucketForMessage(planId, action.getId(), action.getName(), action.getDate());
    }

    @PutMapping("/{planId}/buckets/{id}")
    public PlanMessage updateBucket(
            @PathVariable("planId") long planId,
            @PathVariable("id") long id,
            @RequestBody UpdatePlanBucket action) {
        assert Objects.equals(id, action.getId())
                : String.format("ID mismatch on update bucket (%s on URL, %s in action)",
                                id,
                                action.getId());
        return planService.updateBucketForMessage(planId, action.getId(), action.getName(), action.getDate());
    }

    @DeleteMapping("/{planId}/buckets/{id}")
    public PlanMessage deleteBucket(
            @PathVariable("planId") long planId,
            @PathVariable("id") long id) {
        return planService.deleteBucketForMessage(planId, id);
    }

    // todo: this method is confused. :) it's both create and update?
    @PostMapping("/{id}/acl/grants")
    @ResponseStatus(HttpStatus.CREATED)
    public GrantInfo addGrant(
            @PathVariable("id") Long id,
            @RequestBody GrantInfo grant
    ) {
        Plan plan = planService.setGrantOnPlan(id, grant.getUserId(), grant.getAccessLevel());
        Acl acl = plan.getAcl();
        User user = userRepo.getReferenceById(grant.getUserId());
        return GrantInfo.fromGrant(user, acl.getGrant(user));
    }

    @DeleteMapping("/{id}/acl/grants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeGrant(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId
    ) {
        planService.revokeGrantFromPlan(id, userId);
    }

}
