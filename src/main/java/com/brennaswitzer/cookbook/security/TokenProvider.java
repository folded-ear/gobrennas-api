package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.config.JwtConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTProcessor;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class TokenProvider {

    private AppProperties appProperties;

    private BfsSigningKeyResolver keyResolver;

    @Autowired
    private JWTProcessor<SecurityContext> jwtProcessor;

    public TokenProvider(AppProperties appProperties,
                         BfsSigningKeyResolver keyResolver) {
        this.appProperties = appProperties;
        this.keyResolver = keyResolver;
    }

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        BfsSigningKeyResolver.Key key = keyResolver.getSigningKey();
        DefaultJwsHeader header = new DefaultJwsHeader();
        header.setKeyId(key.id());
        header.setType(Header.JWT_TYPE);
        return Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .setAudience(JwtConfig.BFS_AUDIENCE)
                .setHeader((Map<String, Object>) header)
                .signWith(SignatureAlgorithm.HS512, key.bytes())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
            return Long.parseLong(claimsSet.getSubject());
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new RuntimeException(e);
        }
    }

}
