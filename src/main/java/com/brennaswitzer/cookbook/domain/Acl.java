package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
@Embeddable
public class Acl {

    @NotNull
    @ManyToOne
    private User owner;

    @ElementCollection
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "level_id")
    private Map<User, AccessLevel> grants;

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
        // clear any explicit grant the new owner previously had
        if (grants == null) return;
        grants.remove(owner);
    }

    public Set<User> getGrantedUsers() {
        if (grants == null) {
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
        return grants.keySet();
    }

    public Map<User, AccessLevel> getGrants() {
        if(grants == null) {
            //noinspection unchecked
            return Collections.EMPTY_MAP;
        }
        return grants;
    }

    public AccessLevel getGrant(User user) {
        if (user == null) throw new IllegalArgumentException("The null user can't have an access grant.");
        if (user.equals(owner)) return AccessLevel.ADMINISTER;
        if (grants == null) return null;
        return grants.get(user);
    }

    public AccessLevel setGrant(User user, AccessLevel level) {
        if (user == null) throw new IllegalArgumentException("You can't grant access to the null user.");
        if (user.equals(owner)) throw new UnsupportedOperationException();
        if (grants == null) grants = new HashMap<>();
        return grants.put(user, level);
    }

    public AccessLevel deleteGrant(User user) {
        if (user == null) throw new IllegalArgumentException("You can't revoke access from the null user.");
        if (user.equals(owner)) throw new UnsupportedOperationException();
        if (grants == null) return null;
        return grants.remove(user);
    }

    public boolean isPermitted(User user, AccessLevel level) {
        if (user.equals(owner)) return true;
        AccessLevel grant = getGrant(user);
        if (grant == null) return false;
        return grant.includes(level);
    }
}
