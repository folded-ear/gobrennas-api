package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@DiscriminatorValue("plan")
public class Plan extends PlanItem implements AccessControlled {

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
    private Set<PlanItem> trashBinTasks;

    public Plan() {
    }

    public Plan(String name) {
        super(name);
    }

    public Plan(User owner, String name) {
        super(name);
        setOwner(owner);
    }

    @Override
    public void setParent(PlanItem parent) {
        throw new UnsupportedOperationException("TaskLists can't have parents");
    }

    @Override
    public Plan getTaskList() {
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

    public Set<PlanItem> getTrashBinTasks() {
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
