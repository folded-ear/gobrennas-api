package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Collection;

@Entity
@Getter
@Setter
public class UserDevice extends BaseEntity implements Named {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @NotNull
    @Column(name = "device_key")
    private String key;

    @NotNull
    private String name;

    /**
     * I am the most recent time this device was ensured to exist, which happens
     * any time its preferences are read or updated.
     */
    @NotNull
    private Instant lastEnsuredAt;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private Collection<UserPreference> preferences;

    public void markEnsured() {
        setLastEnsuredAt(Instant.now());
    }

}
