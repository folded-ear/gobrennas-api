package com.brennaswitzer.cookbook.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Set;

@Component
@Slf4j
public class CookieDomainProvider {

    private static final Set<String> TO_STRIP = Set.of(
            "api",
            "next",
            "www");
    private static final String BETA = "beta";

    public void provide(String targetUrl,
                        ResponseCookie.ResponseCookieBuilder cb) {
        String targetHost = URI.create(targetUrl).getHost();
        int idx = targetHost.indexOf('.');
        if (idx < 0) {
            log.info("Set no domain when running locally");
            return;
        }
        String first = targetHost.substring(0, idx);
        if (TO_STRIP.contains(first)) {
            targetHost = targetHost.substring(idx + 1);
        } else if (BETA.equals(first)) {
            int idx2 = targetHost.indexOf('.', idx + 1);
            if (idx2 > 0 && TO_STRIP.contains(targetHost.substring(idx + 1, idx2))) {
                throw new IllegalArgumentException(String.format(
                        "Host '%s' needs to be xxx.%s... not %2$s.xxx...",
                        targetHost,
                        BETA));
            }
        }
        cb.domain(targetHost);
    }

}
