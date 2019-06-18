package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@SuppressWarnings("WeakerAccess")
@Entity
@DiscriminatorValue("list")
public class TaskList extends Task implements AccessControlled {

    @Embedded
    @NotNull
    private Acl acl = new Acl();

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

    @Override
    public TaskList getTaskList() {
        return this;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public User getOwner() {
        return acl.getOwner();
    }

    public void setOwner(User owner) {
        this.acl.setOwner(owner);
    }

}
