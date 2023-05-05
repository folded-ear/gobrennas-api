package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

public class PlanItemCreate extends PlanItemName {

    @Getter
    @Setter
    private Long fromId;

    public boolean hasFromId() {
        return this.fromId != null;
    }

}
