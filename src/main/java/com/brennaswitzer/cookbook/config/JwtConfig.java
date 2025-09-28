package com.brennaswitzer.cookbook.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.mint.ConfigurableJWSMinter;
import com.nimbusds.jose.mint.DefaultJWSMinter;
import com.nimbusds.jose.mint.JWSMinter;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.List;
import java.util.Set;

@Configuration
public class JwtConfig {

    public static final String BFS_AUDIENCE = "BFS API";

    @Bean
    public JWKSet jwkSet(AppProperties appProperties) {
        List<AppProperties.Secret> tokenSecrets = appProperties.getAuth()
                .getTokenSecrets();
        if (tokenSecrets == null || tokenSecrets.isEmpty()) {
            throw new IllegalStateException("At least one token secret must be provided in app.auth.token-secrets[]");
        }
        Base64.Decoder decoder = Base64.getDecoder();
        return new JWKSet(
                tokenSecrets
                        .stream()
                        .map(s -> (JWK) new OctetSequenceKey.Builder(
                                decoder.decode(s.getSecret())
                        )
                                .keyID(s.getId())
                                .build())
                        .toList());
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(JWKSet jwkSet) {
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }

    /**
     * I can mint new BFS tokens (i.e., {@code FTOKEN}), based on a header and
     * claims set.
     */
    @Bean
    public JWSMinter<SecurityContext> jwtMinter(
            JWKSource<SecurityContext> jwkSource) {
        ConfigurableJWSMinter<SecurityContext> minter = new DefaultJWSMinter<>();
        minter.setJWKSource(jwkSource);
        return minter;
    }

    /**
     * I can process BFS tokens (i.e., {@code FTOKEN}) from their compact form
     * to a ready-to-use claims set.
     */
    @Bean
    public JWTProcessor<SecurityContext> jwtProcessor(
            JWKSource<SecurityContext> jwkSource) {

        // Create a JWT processor for the access tokens
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        // Set the required "typ" header
        jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(
                JOSEObjectType.JWT));

        // Configure a key selector to find secrets, via the "kid" header.
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(
                Set.of(JWSAlgorithm.HS512),
                jwkSource));

        // Configure required claims
        jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                // value match
                new JWTClaimsSet.Builder()
                        .audience(BFS_AUDIENCE)
                        .build(),
                // existence
                Set.of(JWTClaimNames.SUBJECT,
                       JWTClaimNames.ISSUED_AT,
                       JWTClaimNames.EXPIRATION_TIME)));

        return jwtProcessor;
    }

}
