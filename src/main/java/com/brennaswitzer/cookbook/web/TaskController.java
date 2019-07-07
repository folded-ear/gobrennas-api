package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.*;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.services.TaskService;
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

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskInfo> getTaskLists(
    ) {
        return TaskInfo.fromLists(taskService.getTaskLists());
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskInfo createTaskList(
            @RequestBody TaskName info
    ) {
        return TaskInfo.fromList(
                taskService.createTaskList(info.getName())
        );
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

    @PostMapping("/{id}/subtasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskInfo createSubtask(
            @PathVariable("id") Long parentId,
            @RequestParam(name = "after", required = false) Long afterId,
            @RequestBody TaskName info
    ) {
        return TaskInfo.fromTask(afterId == null
                ? taskService.createSubtask(parentId, info.getName())
                : taskService.createSubtaskAfter(parentId, info.getName(), afterId)
        );
    }

    @PutMapping("/{id}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void renameTask(
            @PathVariable("id") Long id,
            @RequestBody TaskName info
    ) {
        taskService.renameTask(id, info.getName());
    }

    @PutMapping("/{id}/subtaskIds")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetSubtasks(
            @PathVariable("id") Long id,
            @RequestBody SubtaskIds info
    ) {
        taskService.resetSubtasks(id, info.getSubtaskIds());
    }

    @PutMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void completeTask(
            @PathVariable("id") Long id
    ) {
        taskService.completeTask(id);
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
