package com.brennaswitzer.cookbook.payload;

import javax.validation.constraints.NotBlank;

public class TaskName {

    @NotBlank
    private String name;

    public TaskName() {
    }

    public TaskName(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
