package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.*;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.TaskService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public List<TaskInfo> getTaskLists(
    ) {
        return TaskInfo.fromLists(taskService.getTaskLists());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public TaskInfo createTaskList(@RequestBody TaskCreate info) {
        TaskList taskList;
        if (info.hasFromId()) {
            taskList = taskService.duplicateTaskList(info.getName(), info.getFromId());
        } else {
            taskList = taskService.createTaskList(info.getName());
        }
        taskList.setOwner(principalAccess.getUser());
        return TaskInfo.fromList(taskList);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TaskInfo getTask(
            @PathVariable("id") Long id
    ) {
        return TaskInfo.fromTask(taskService.getTaskById(id));
    }

    @GetMapping("/{id}/subtasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskInfo> getSubtasks(
            @PathVariable("id") Long parentId
    ) {
        return TaskInfo.fromTasks(taskService
                .getTaskById(parentId)
                .getOrderedSubtasksView());
    }

    @PutMapping("/{id}/name")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public TaskInfo renameTask(
            @PathVariable("id") Long id,
            @RequestBody TaskName info
    ) {
        return TaskInfo.fromTask(taskService
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
