package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.security.UserPrincipal;

import java.util.Optional;

public interface UserPrincipalAccess {

    default Long getId() {
        return getUserPrincipal().getId();
    }

    Optional<UserPrincipal> findUserPrincipal();

    default UserPrincipal getUserPrincipal() {
        return findUserPrincipal()
                .orElseThrow(NoUserPrincipalException::new);
    }

    default User getUser() {
        return getUser(getUserPrincipal());
    }

    default User getUser(UserPrincipal principal) {
        throw new UnsupportedOperationException();
    }

}
