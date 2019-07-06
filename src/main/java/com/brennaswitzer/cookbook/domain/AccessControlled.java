package com.brennaswitzer.cookbook.domain;

import org.springframework.security.access.AccessDeniedException;

public interface AccessControlled extends Owned {

    Acl getAcl();

    default User getOwner() {
        return getAcl().getOwner();
    }

    default void setOwner(User owner) {
        getAcl().setOwner(owner);
    }

    default boolean isPermitted(User user, AccessLevel level) {
        Acl acl = getAcl();
        if (acl == null) return false;
        return acl.isPermitted(user, level);
    }

    default void ensurePermitted(User user, AccessLevel level) {
        if (! isPermitted(user, level)) {
            throw new AccessDeniedException("Unauthorized");
        }
    }

}
