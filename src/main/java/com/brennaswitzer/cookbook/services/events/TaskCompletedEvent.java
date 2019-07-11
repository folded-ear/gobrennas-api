package com.brennaswitzer.cookbook.services.events;

import com.brennaswitzer.cookbook.domain.Task;

public class TaskCompletedEvent {

    private Long id;

    private Task task;

    public TaskCompletedEvent(Long id) {
        setId(id);
    }

    public TaskCompletedEvent(Task task) {
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
    }
}
