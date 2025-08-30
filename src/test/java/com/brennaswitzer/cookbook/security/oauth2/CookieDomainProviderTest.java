package com.brennaswitzer.cookbook.security.oauth2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CookieDomainProviderTest {

    @Mock
    private ResponseCookie.ResponseCookieBuilder builder;

    @Test
    void localhost() {
        new CookieDomainProvider().provide(
                "http://localhost:4000//post-oauth2/redirect",
                builder);

        verify(builder, never()).domain(any());
    }

    @Test
    void prod() {
        new CookieDomainProvider().provide(
                "http://api.gobrennas.com/post-oauth2/redirect",
                builder);

        verify(builder).domain("gobrennas.com");
    }

    @Test
    void oldBeta() {
        CookieDomainProvider provider = new CookieDomainProvider();

        assertThrows(IllegalArgumentException.class,
                     () -> provider.provide(
                             "http://beta.api.gobrennas.com//post-oauth2/redirect",
                             builder));
    }

    @Test
    void targetBeta() {
        new CookieDomainProvider().provide(
                "http://api.beta.gobrennas.com//post-oauth2/redirect",
                builder);

        verify(builder).domain("beta.gobrennas.com");
    }

}
