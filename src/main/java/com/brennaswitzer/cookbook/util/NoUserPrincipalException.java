package com.brennaswitzer.cookbook.util;

import org.springframework.security.core.AuthenticationException;

public class NoUserPrincipalException extends AuthenticationException {

    public NoUserPrincipalException() {
        this("No user principal found. Are you logged in?");
    }

    public NoUserPrincipalException(String msg) {
        super(msg);
    }

}
