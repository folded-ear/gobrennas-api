package com.brennaswitzer.cookbook.security;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest
    @CsvSource({
            "https://gobrennas.com//post-oauth2/redirect,          gobrennas.com",
            "https://www.gobrennas.com//post-oauth2/redirect,      gobrennas.com",
            "https://next.gobrennas.com//post-oauth2/redirect,     gobrennas.com",
            "https://api.gobrennas.com//post-oauth2/redirect,      gobrennas.com",
            "https://beta.gobrennas.com/post-oauth2/redirect,      beta.gobrennas.com",
            "https://www.beta.gobrennas.com/post-oauth2/redirect,  beta.gobrennas.com",
            "https://next.beta.gobrennas.com/post-oauth2/redirect, beta.gobrennas.com",
            "https://api.beta.gobrennas.com/post-oauth2/redirect,  beta.gobrennas.com",
    })
    void withDomain(String target, String domain) {
        new CookieDomainProvider()
                .provide(target, builder);

        verify(builder).domain(domain);
    }

    @ParameterizedTest
    @CsvSource({
            "https://localhost:4000/post-oauth2/redirect",
    })
    void withoutDomain(String target) {
        new CookieDomainProvider()
                .provide(target, builder);

        verify(builder, never()).domain(any());
    }

    @ParameterizedTest
    @CsvSource({
            "https://beta.www.gobrennas.com/post-oauth2/redirect",
            "https://beta.next.gobrennas.com/post-oauth2/redirect",
            "https://beta.api.gobrennas.com/post-oauth2/redirect",
    })
    void disallowed(String target) {
        var p = new CookieDomainProvider();

        assertThrows(IllegalArgumentException.class,
                     () -> p.provide(target, builder));
    }

}
