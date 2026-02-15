package com.brennaswitzer.cookbook.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

public class HeaderTokenAuthenticationFilter extends AbstractTokenAuthenticationFilter {

    private static final String BEARER_AUTHENTICATION_PREFIX = "Bearer ";
    private static final int PREFIX_LENGTH = BEARER_AUTHENTICATION_PREFIX.length();

    protected String getJwtFromRequest(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization)
            && authorization.length() > PREFIX_LENGTH
            && authorization.substring(0, PREFIX_LENGTH)
                    .equalsIgnoreCase(BEARER_AUTHENTICATION_PREFIX)
        ) {
            return authorization.substring(PREFIX_LENGTH)
                    .strip();
        }
        return null;
    }

}
