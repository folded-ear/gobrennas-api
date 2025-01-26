package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Collection;

@Setter
@Getter
@Entity
@Table(name = "plan_bucket", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "plan_id", "name" })
})
public class PlanBucket extends BaseEntity implements Named {

    /**
     * Annotated {@code @NotNull} as it's never <em>valid</em> for a bucket's
     * plan to be {@code null}, even though the field may briefly be {@code
     * null} after being removed from its (former) plan, but before orphan
     * removal at session flush.
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private Plan plan;

    private String name;

    private LocalDate date;

    @OneToMany(mappedBy = "bucket")
    private Collection<PlanItem> items;

    @Column(name = "mod_count")
    private int modCount;

    public PlanBucket() {}

    public PlanBucket(Plan plan, String name, LocalDate date) {
        setPlan(plan);
        setName(name);
        setDate(date);
    }

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        setModCount(1);
    }

    @PreUpdate
    protected void onPreUpdate() {
        setModCount(getModCount() + 1);
    }

    @SuppressWarnings("unused")
    public boolean isDated() {
        return date != null;
    }

    public boolean isNamed() {
        return name != null && !name.isBlank();
    }

    public void setPlan(Plan plan) {
        if (this.plan != null) {
            this.plan.getBuckets().remove(this);
            this.items.forEach(it -> it.setBucket(null));
            this.plan.markDirty(); // for change detection
        }
        this.plan = plan;
        if (this.plan != null) {
            this.plan.getBuckets().add(this);
            this.plan.markDirty(); // for change detection
        }
    }

    public PlanBucket of(Plan plan) {
        setPlan(plan);
        return this;
    }

}
