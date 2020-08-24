package com.brennaswitzer.cookbook.payload;

import javax.validation.constraints.NotNull;

public class ParentId {

    @NotNull
    private Long parentId;

    public ParentId() {
    }

    public ParentId(@NotNull Long parentId) {
        setParentId(parentId);
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

}
