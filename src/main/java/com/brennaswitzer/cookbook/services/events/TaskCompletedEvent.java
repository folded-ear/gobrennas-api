package com.brennaswitzer.cookbook.services.events;

public class TaskCompletedEvent {

    private Long id;

    public TaskCompletedEvent(Long id) {
        setId(id);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
