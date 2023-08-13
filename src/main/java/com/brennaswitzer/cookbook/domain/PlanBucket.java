package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Collection;

@Entity
@Table(name = "plan_bucket", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "plan_id", "name" })
})
public class PlanBucket extends BaseEntity {

    @NotNull
    @Getter
    @Setter
    @ManyToOne
    private Plan plan;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private LocalDate date;

    @Getter
    @Setter
    @OneToMany(mappedBy = "bucket")
    private Collection<PlanItem> items;

    @Getter
    @Setter
    @Column(name = "mod_count")
    private int modCount;

    public PlanBucket() {}

    public PlanBucket(String name) {
        setName(name);
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

    public boolean isDated() {
        return date != null;
    }

    public boolean isNamed() {
        return name != null && !name.isBlank();
    }

}
