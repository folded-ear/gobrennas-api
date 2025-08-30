package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import static com.brennaswitzer.cookbook.security.CookieTokenAuthenticationFilter.TOKEN_COOKIE_NAME;

@Component
public class CookieTokenLogoutHandler implements LogoutHandler {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private CookieDomainProvider cookieDomainProvider;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {
        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .path("/")
                .maxAge(0)
                .secure(request.isSecure());
        cookieDomainProvider.provide(appProperties.getPublicUrl(), cb);
        CookieUtils.addCookie(response, cb);
    }

}
