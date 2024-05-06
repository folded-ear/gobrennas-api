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
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.Collection;

@Entity
@Table(name = "plan_bucket", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "plan_id", "name" })
})
@Getter
@Setter
public class PlanBucket extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Plan plan;

    @Audited
    private String name;

    @Audited
    private LocalDate date;

    @OneToMany(mappedBy = "bucket")
    private Collection<PlanItem> items;

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
