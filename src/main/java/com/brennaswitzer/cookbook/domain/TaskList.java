package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Entity
@DiscriminatorValue("list")
public class TaskList extends Task implements AccessControlled {

    @Embedded
    @NotNull
    @Getter
    @Setter
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

}
