package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Preference extends BaseEntity implements Named {

    public static final String PREF_ACTIVE_PLAN = "activePlan";
    public static final String PREF_ACTIVE_SHOPPING_PLANS = "activeShoppingPlans";

    @NotNull
    private String name;

    @NotNull
    @Column(columnDefinition = "int4")
    private DataType type;

    @Column(name = "default_value_str")
    private String defaultValue;

}
