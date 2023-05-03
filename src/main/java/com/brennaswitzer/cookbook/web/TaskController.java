package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.AclInfo;
import com.brennaswitzer.cookbook.payload.GrantInfo;
import com.brennaswitzer.cookbook.payload.PlanItemInfo;
import com.brennaswitzer.cookbook.payload.TaskCreate;
import com.brennaswitzer.cookbook.payload.TaskName;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.TaskService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
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
    private UserRepository userRepo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanItemInfo> getTaskLists(
    ) {
        return PlanItemInfo.fromLists(taskService.getTaskLists());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PlanItemInfo createTaskList(@RequestBody TaskCreate info) {
        TaskList taskList;
        if (info.hasFromId()) {
            taskList = taskService.duplicateTaskList(info.getName(), info.getFromId());
        } else {
            taskList = taskService.createTaskList(info.getName());
        }
        taskList.setOwner(principalAccess.getUser());
        return PlanItemInfo.fromList(taskList);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PlanItemInfo getTask(
            @PathVariable("id") Long id
    ) {
        return PlanItemInfo.fromPlanItem(taskService.getTaskById(id));
    }

    @GetMapping("/{id}/subtasks")
    @ResponseStatus(HttpStatus.OK)
    public List<PlanItemInfo> getSubtasks(
            @PathVariable("id") Long parentId
    ) {
        return PlanItemInfo.fromTasks(taskService
                                              .getTaskById(parentId)
                                              .getOrderedSubtasksView());
    }

    @PutMapping("/{id}/name")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PlanItemInfo renameTask(
            @PathVariable("id") Long id,
            @RequestBody TaskName info
    ) {
        return PlanItemInfo.fromPlanItem(taskService
                                                 .renameTask(id, info.getName()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable("id") Long id
    ) {
        taskService.deleteTask(id);
    }

    @GetMapping("/{id}/acl")
    @ResponseStatus(HttpStatus.OK)
    public AclInfo getListAcl(
            @PathVariable("id") Long id
    ) {
        TaskList list = taskService.getTaskListById(id);
        return AclInfo.fromAcl(list.getAcl());
    }

    // todo: this method is confused. :) it's both create and update?
    @PostMapping("/{id}/acl/grants")
    @ResponseStatus(HttpStatus.CREATED)
    public GrantInfo addGrant(
            @PathVariable("id") Long id,
            @RequestBody GrantInfo grant
    ) {
        TaskList list = taskService.setGrantOnList(id, grant.getUserId(), grant.getAccessLevel());
        Acl acl = list.getAcl();
        User user = userRepo.getById(grant.getUserId());
        return GrantInfo.fromGrant(user, acl.getGrant(user));
    }

    @DeleteMapping("/{id}/acl/grants/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGrant(
            @PathVariable("id") Long id,
            @PathVariable("userId") Long userId
    ) {
        taskService.deleteGrantFromList(id, userId);
    }

}
