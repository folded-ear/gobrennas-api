package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@SuppressWarnings("WeakerAccess")
@Entity
@DiscriminatorValue("list")
public class TaskList extends Task {

    @Embedded
    @NotNull
    private Acl acl;

    public TaskList() {}

    public TaskList(String name) {
        super(name);
    }

    public TaskList(User owner, String name) {
        super(name);
        setOwner(owner);
    }

    @Override
    public void setParent(Task parent) {
        throw new UnsupportedOperationException("TaskLists can't have parents");
    }

    public User getOwner() {
        if (acl == null) return null;
        return acl.getOwner();
    }

    public void setOwner(User owner) {
        if (acl == null) acl = new Acl();
        this.acl.setOwner(owner);
    }

}
