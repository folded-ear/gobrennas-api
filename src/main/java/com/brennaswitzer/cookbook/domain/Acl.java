package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
@Embeddable
public class Acl {

    @NotNull
    @ManyToOne
    private User owner;

    @ElementCollection
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "perm")
    @Enumerated(EnumType.STRING)
    private Map<User, Permission> grants;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Optional<Permission> getGrant(User user) {
        if (! grants.containsKey(user)) return Optional.empty();
        return Optional.of(grants.get(user));
    }

    public Optional<Permission> setGrant(User user, Permission perm) {
        Optional<Permission> prior = getGrant(user);
        grants.put(user, perm);
        return prior;
    }

    public Optional<Permission> removeGrant(User user) {
        Optional<Permission> prior = getGrant(user);
        grants.remove(user);
        return prior;
    }

}
