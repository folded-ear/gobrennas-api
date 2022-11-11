package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("plan")
public class TaskList extends Task implements AccessControlled {

    @Embedded
    @NotNull
    @Getter
    @Setter
    private Acl acl = new Acl();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<PlanBucket> buckets;

    @OneToMany(mappedBy = "trashBin", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    private Set<Task> trashBinTasks;

    public TaskList() {
    }

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

    public Set<PlanBucket> getBuckets() {
        if (buckets == null) {
            buckets = new HashSet<>();
        }
        return buckets;
    }

    public boolean hasBuckets() {
        return buckets != null && !buckets.isEmpty();
    }

    public Set<Task> getTrashBinTasks() {
        if (trashBinTasks == null) {
            trashBinTasks = new HashSet<>();
        }
        return trashBinTasks;
    }

    public boolean hasTrash() {
        return trashBinTasks != null && !trashBinTasks.isEmpty();
    }

    public User getOwner() {
        return getAcl().getOwner();
    }

}
