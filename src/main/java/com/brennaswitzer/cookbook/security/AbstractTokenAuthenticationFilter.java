package com.brennaswitzer.cookbook.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public abstract class AbstractTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String STACKDRIVER_UPTIME_USER_AGENT = "GoogleStackdriverMonitoring-UptimeChecks";

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.USER_AGENT))
                .map(s -> s.startsWith(STACKDRIVER_UPTIME_USER_AGENT))
                .orElse(false);
    }

    @Override
    protected final void doFilterInternal(HttpServletRequest request,
                                          HttpServletResponse response,
                                          FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            log.debug("consult {} for a token", this);
            String jwt = null;
            try {
                jwt = getJwtFromRequest(request);

                if (StringUtils.hasText(jwt)) {
                    Long userId = tokenProvider.getUserIdFromToken(jwt);

                    UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ex) {
                log.warn("Failed to read token '" + jwt + "'", ex);
            }
        } else {
            log.debug("already have an authentication");
        }

        filterChain.doFilter(request, response);
    }

    protected abstract String getJwtFromRequest(HttpServletRequest request);
}
