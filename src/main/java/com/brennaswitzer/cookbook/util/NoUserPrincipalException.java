package com.brennaswitzer.cookbook.util;

import org.springframework.security.core.AuthenticationException;

public class NoUserPrincipalException extends AuthenticationException {

    public NoUserPrincipalException(String msg) {
        super(msg);
    }

}
