package com.brennaswitzer.cookbook.security;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class HeaderTokenAuthenticationFilter extends AbstractTokenAuthenticationFilter {

    private static final String BEARER_AUTHENTICATION_PREFIX = "Bearer ";

    protected String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_AUTHENTICATION_PREFIX)) {
            return bearerToken.substring(BEARER_AUTHENTICATION_PREFIX.length());
        }
        return null;
    }

}
