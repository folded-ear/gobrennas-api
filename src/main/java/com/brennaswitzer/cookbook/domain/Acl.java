package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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
        // clear any explicit grant the new owner previously had
        if (grants == null) return;
        grants.remove(owner);
    }

    public Permission getGrant(User user) {
        if (user == owner) return Permission.ADMINISTER;
        if (grants == null) return null;
        return grants.get(user);
    }

    public Permission setGrant(User user, Permission perm) {
        if (user == owner) throw new UnsupportedOperationException();
        if (grants == null) grants = new HashMap<>();
        return grants.put(user, perm);
    }

    public Permission removeGrant(User user) {
        if (user == owner) throw new UnsupportedOperationException();
        if (grants == null) return null;
        return grants.remove(user);
    }

}
