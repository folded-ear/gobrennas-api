package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.SubtaskIds;
import com.brennaswitzer.cookbook.payload.TaskInfo;
import com.brennaswitzer.cookbook.payload.TaskName;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
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
    private UserRepository userRepository;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskInfo> getRootTasks(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return TaskInfo.fromTasks(taskService.getRootTasks(
                userRepository.getById(userPrincipal.getId())
        ));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskInfo createTask(
            @RequestBody TaskName info,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return TaskInfo.fromTask(
                taskService.createRootTask(info.getName(), userRepository.getById(userPrincipal.getId()))
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @PathVariable("id") Long id
    ) {
        taskService.deleteTask(id);
    }

}
