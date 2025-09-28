package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.jsonwebtoken.impl.TextCodec;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * I can resolve symmetric keys for signing/verifying JWTs. I'm a stopgap on the
 * way to replacing jjwt with nimbus-jose-jwt, so rotation can be added now.
 */
@Component
public class BfsSigningKeyResolver extends SigningKeyResolverAdapter {

    public record Key(String id,
                      byte[] bytes) {}

    public static final String ANONYMOUS_KEY_ID = "?";

    @Getter
    private Key signingKey;
    private Map<String, byte[]> keyBytesById;

    @Autowired
    public void setAppProperties(AppProperties appProperties) {
        val auth = appProperties.getAuth();
        val keys = new ArrayList<Key>();
        // named secrets take priority
        if (auth.getTokenSecrets() != null) {
            Base64.Decoder decoder = Base64.getDecoder();
            auth.getTokenSecrets()
                    .stream()
                    .map(s -> new Key(
                            s.getId(),
                            decoder.decode(s.getSecret())))
                    .forEach(keys::add);
        }
        // support the old anonymous secret, if present
        if (auth.getTokenSecret() != null) {
            // this uses more lenient decoding to match jjwt
            keys.add(new Key(
                    ANONYMOUS_KEY_ID,
                    TextCodec.BASE64.decode(auth.getTokenSecret())));
        }
        if (keys.isEmpty()) {
            throw new IllegalStateException("At least one token secret must be provided");
        }
        // always sign with the first key
        signingKey = keys.get(0);
        // support verifying with any key
        keyBytesById = new HashMap<>();
        for (var k : keys) {
            if (keyBytesById.containsKey(k.id())) {
                throw new IllegalStateException("Multiple token secrets with id '" + k.id() + "' are present.");
            }
            keyBytesById.put(k.id(), k.bytes());
        }
    }

    @Override
    public byte[] resolveSigningKeyBytes(JwsHeader header, Claims claims) {
        String id = header.getKeyId();
        // if no keyId is provided, assume anonymous
        if (id == null) id = ANONYMOUS_KEY_ID;
        // If the id is unknown, use the signing key
        return keyBytesById.getOrDefault(id, signingKey.bytes());
    }

}
