package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.util.CookieUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieTokenAuthenticationFilter extends AbstractTokenAuthenticationFilter {

    public static final String TOKEN_COOKIE_NAME = "FTOKEN";

    protected String getJwtFromRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, TOKEN_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(null);
    }

}
