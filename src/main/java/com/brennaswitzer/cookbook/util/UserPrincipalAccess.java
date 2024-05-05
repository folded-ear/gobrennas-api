package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.security.UserPrincipal;

public interface UserPrincipalAccess {

    default Long getId() {
        return getUserPrincipal().getId();
    }

    default String getUsername() {
        return getUserPrincipal().getUsername();
    }

    UserPrincipal getUserPrincipal();

    default User getUser() {
        throw new UnsupportedOperationException();
    }

}
