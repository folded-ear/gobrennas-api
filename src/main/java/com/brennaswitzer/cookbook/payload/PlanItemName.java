package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class PlanItemName {

    @NonNull
    @Getter
    @Setter
    private String name;

    public PlanItemName() {
    }

    public PlanItemName(String name) {
        setName(name);
    }

}
