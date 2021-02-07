package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class TaskName {

    @NonNull
    @Getter
    @Setter
    private String name;

    public TaskName() {
    }

    public TaskName(String name) {
        setName(name);
    }

}
