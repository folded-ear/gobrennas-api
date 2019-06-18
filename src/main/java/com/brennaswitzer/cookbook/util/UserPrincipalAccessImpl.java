package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserPrincipalAccessImpl implements UserPrincipalAccess {

    @Override
    public UserPrincipal getUserPrincipal() {
        return (UserPrincipal) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

}
