package com.brennaswitzer.cookbook.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParentId {

    @NotNull
    private Long parentId;

    public ParentId() {
    }

    public ParentId(@NotNull Long parentId) {
        setParentId(parentId);
    }

}
