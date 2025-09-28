package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class TokenProvider {

    private static final String BFS_AUDIENCE = "BFS API";

    private AppProperties appProperties;

    private BfsSigningKeyResolver keyResolver;

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
                .setAudience(BFS_AUDIENCE)
                .setHeader((Map<String, Object>) header)
                .signWith(SignatureAlgorithm.HS512, key.bytes())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKeyResolver(keyResolver)
                .parseClaimsJws(token);

        JwsHeader<?> header = jws.getHeader();
        if (header.getType() != null && !Header.JWT_TYPE.equals(header.getType())) {
            throw new UnsupportedJwtException("Unrecognized '" + header.getType() + "' token type.");
        }

        Claims claims = jws.getBody();
        if (claims.getAudience() != null && !BFS_AUDIENCE.equals(claims.getAudience())) {
            throw new UnsupportedJwtException("Unrecognized '" + claims.getAudience() + "' audience claim.");
        }

        return Long.parseLong(claims.getSubject());
    }

}
