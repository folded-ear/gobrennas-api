package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserPreference extends BaseEntity implements Named {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserDevice device;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Preference preference;

    @Column(name = "value_str") // H2 doesn't allow a column named 'value'
    private String value;

    @Override
    public String getName() {
        return preference.getName();
    }

    public DataType getType() {
        return preference.getType();
    }

}
