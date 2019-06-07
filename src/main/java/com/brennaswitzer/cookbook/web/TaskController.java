package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("")
    public Iterable<Task> getRootTasks() {
        return taskService.findRootTasks();
    }

}
