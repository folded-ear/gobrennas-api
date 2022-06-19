package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

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
