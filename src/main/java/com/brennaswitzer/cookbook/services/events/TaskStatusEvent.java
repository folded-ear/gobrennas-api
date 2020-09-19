package com.brennaswitzer.cookbook.services.events;

import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskStatus;

public class TaskStatusEvent {

    private Long id;

    private Task task;

    private TaskStatus status;

    public TaskStatusEvent(Task task) {
        setTask(task);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        setId(task.getId());
        this.task = task;
        setStatus(task.getStatus());
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
