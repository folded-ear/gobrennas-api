package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BfsSigningKeyResolverTest {

    private BfsSigningKeyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new BfsSigningKeyResolver();
        val appProps = new AppProperties();
        val auth = appProps.getAuth();
        auth.setTokenSecret("anon");
        auth.setTokenSecrets(List.of(
                new AppProperties.Secret("a", "keyA"),
                new AppProperties.Secret("z", "keyZ")));
        resolver.setAppProperties(appProps);
    }

    @Test
    void signingKey_firstSecret() {
        BfsSigningKeyResolver.Key key = resolver.getSigningKey();

        assertEquals("a",
                     key.id());
        assertArrayEquals(decode("keyA"),
                          key.bytes());
    }

    @Test
    void signingKey_noSecrets() {
        resolver = new BfsSigningKeyResolver();
        val appProps = new AppProperties();
        val auth = appProps.getAuth();
        auth.setTokenSecret("anon");
        resolver.setAppProperties(appProps);

        BfsSigningKeyResolver.Key key = resolver.getSigningKey();

        assertEquals(BfsSigningKeyResolver.ANONYMOUS_KEY_ID,
                     key.id());
        assertArrayEquals(decode("anon"),
                          key.bytes());
    }

    @Test
    void signingKey_onlySecrets() {
        resolver = new BfsSigningKeyResolver();
        val appProps = new AppProperties();
        val auth = appProps.getAuth();
        auth.setTokenSecrets(List.of(
                new AppProperties.Secret("a", "keyA")));
        resolver.setAppProperties(appProps);

        BfsSigningKeyResolver.Key key = resolver.getSigningKey();

        assertEquals("a",
                     key.id());
        assertArrayEquals(decode("keyA"),
                          key.bytes());
    }

    @Test
    void noKeyId() {
        assertArrayEquals(decode("anon"),
                          resolver.resolveSigningKeyBytes(
                                  new DefaultJwsHeader(),
                                  new DefaultClaims()));
    }

    @Test
    void anonymousKey() {
        assertArrayEquals(decode("anon"),
                          resolver.resolveSigningKeyBytes(
                                  new DefaultJwsHeader()
                                          .setKeyId(BfsSigningKeyResolver.ANONYMOUS_KEY_ID),
                                  new DefaultClaims()));
    }

    @Test
    void keyA() {
        assertArrayEquals(decode("keyA"),
                          resolver.resolveSigningKeyBytes(
                                  new DefaultJwsHeader()
                                          .setKeyId("a"),
                                  new DefaultClaims()));
    }

    @Test
    void keyZ() {
        assertArrayEquals(decode("keyZ"),
                          resolver.resolveSigningKeyBytes(
                                  new DefaultJwsHeader()
                                          .setKeyId("z"),
                                  new DefaultClaims()));
    }

    @Test
    void unknownKey() {
        assertArrayEquals(decode("keyA"),
                          resolver.resolveSigningKeyBytes(
                                  new DefaultJwsHeader()
                                          .setKeyId("unknown"),
                                  new DefaultClaims()));
    }

    private static byte[] decode(String s) {
        return Base64.getDecoder().decode(s);
    }

}
