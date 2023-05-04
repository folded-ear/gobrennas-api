package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.AclInfo;
import com.brennaswitzer.cookbook.payload.GrantInfo;
import com.brennaswitzer.cookbook.payload.PlanItemCreate;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.services.TaskService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/tasks")
@PreAuthorize("hasRole('USER')")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanItemInfo> getPlans() {
        return PlanItemInfo.fromPlans(planService.getPlans());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlanItemInfo createPlan(@RequestBody PlanItemCreate info) {
        Plan plan;
        if (info.hasFromId()) {
            plan = planService.duplicatePlan(info.getName(), info.getFromId());
        } else {
            plan = planService.createPlan(info.getName());
        }
        plan.setOwner(principalAccess.getUser());
        return PlanItemInfo.fromPlan(plan);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanItemInfo getPlanItem(
            @PathVariable("id") Long id
    ) {
        return PlanItemInfo.fromPlanItem(planService.getPlanItemById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(
            @PathVariable("id") Long id
    ) {
        taskService.deleteItem(id);
    }

    @GetMapping("/{id}/acl")
    @ResponseStatus(HttpStatus.OK)
    public AclInfo getPlanAcl(
            @PathVariable("id") Long id
    ) {
        Plan plan = planService.getPlanById(id);
        return AclInfo.fromAcl(plan.getAcl());
    }

    // todo: this method is confused. :) it's both create and update?
    @PostMapping("/{id}/acl/grants")
    @ResponseStatus(HttpStatus.CREATED)
    public GrantInfo addGrant(
            @PathVariable("id") Long id,
            @RequestBody GrantInfo grant
    ) {
        Plan plan = taskService.setGrantOnPlan(id, grant.getUserId(), grant.getAccessLevel());
        Acl acl = plan.getAcl();
        User user = userRepo.getById(grant.getUserId());
        return GrantInfo.fromGrant(user, acl.getGrant(user));
    }

    @DeleteMapping("/{id}/acl/grants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGrant(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId
    ) {
        taskService.deleteGrantFromPlan(id, userId);
    }

}
