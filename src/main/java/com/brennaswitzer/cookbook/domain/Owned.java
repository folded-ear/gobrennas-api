package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.security.UserPrincipal;

/**
 * @author bboisvert
 */
public interface Owned {

    User getOwner();

    void setOwner(User owner);

    default boolean isOwner(User user) {
        return isOwner(user.getId());
    }

    default boolean isOwner(UserPrincipal up) {
        return isOwner(up.getId());
    }

    default boolean isOwner(long userId) {
        return userId == getOwner().getId();
    }

}
