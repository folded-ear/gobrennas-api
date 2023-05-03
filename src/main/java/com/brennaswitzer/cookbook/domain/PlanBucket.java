package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

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

    public PlanBucket() {}

    public PlanBucket(String name) {
        setName(name);
    }

}
