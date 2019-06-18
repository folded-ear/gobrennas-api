package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.security.UserPrincipal;

public interface UserPrincipalAccess {

    default Long getId() {
        return getUserPrincipal().getId();
    }

    UserPrincipal getUserPrincipal();

}
