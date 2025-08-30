package com.brennaswitzer.cookbook.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@Slf4j
public class CookieDomainProvider {

    /**
     * If the host starts with this, set the token cookie without it.
     */
    private static final String API_HOST_PREFIX = "api.";
    private static final String BETA_HOST_PREFIX = "beta.";

    public void provide(String targetUrl,
                        ResponseCookie.ResponseCookieBuilder cb) {
        String targetHost = URI.create(targetUrl).getHost();
        if ("localhost".equals(targetHost)) {
            log.info("Set no domain when running locally");
        } else if (targetHost.startsWith(API_HOST_PREFIX)) {
            cb.domain(targetHost.substring(API_HOST_PREFIX.length()));
        } else if (targetHost.startsWith(BETA_HOST_PREFIX)) {
            throw new IllegalArgumentException(String.format(
                    "Beta API (%s) needs to be %s%s.. not %s%s..",
                    targetHost,
                    API_HOST_PREFIX,
                    BETA_HOST_PREFIX,
                    BETA_HOST_PREFIX,
                    API_HOST_PREFIX));
        } else {
            log.warn("Unrecognized '{}' host; set no domain on cookie", targetHost);
        }
    }

}
