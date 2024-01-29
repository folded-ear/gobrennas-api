package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
public class AppSetting extends BaseEntity implements Named {

    @NotNull
    @Column(updatable = false)
    private String name;
    @NotNull
    private DataType type;
    @Column(name = "value_str") // H2 doesn't allow a column named 'value'
    private String value;

    public AppSetting() {
    }

    public AppSetting(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    public Duration getAgeOfValue() {
        return Duration.between(getUpdatedAt(),
                                Instant.now());
    }

}