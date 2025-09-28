package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.config.JwtConfig;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.mint.JWSMinter;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;

@Service
@Slf4j
public class TokenProvider {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private JWTProcessor<SecurityContext> jwtProcessor;

    @Autowired
    private JWSMinter<SecurityContext> jwtMinter;

    public String createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.getAuth().getTokenExpirationMsec());

        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();
            Payload payload = new JWTClaimsSet.Builder()
                    .subject(Long.toString(userPrincipal.getId()))
                    .issueTime(now)
                    .expirationTime(expiryDate)
                    .audience(JwtConfig.BFS_AUDIENCE)
                    .build()
                    .toPayload();
            return jwtMinter.mint(header, payload, null)
                    .serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
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
