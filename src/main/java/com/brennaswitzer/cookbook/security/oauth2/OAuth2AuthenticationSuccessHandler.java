package com.brennaswitzer.cookbook.security.oauth2;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.exceptions.BadRequestException;
import com.brennaswitzer.cookbook.security.TokenProvider;
import com.brennaswitzer.cookbook.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.brennaswitzer.cookbook.security.CookieTokenAuthenticationFilter.TOKEN_COOKIE_NAME;
import static com.brennaswitzer.cookbook.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    /**
     * If the host header starts with this, set the token cookie without it.
     */
    private static final String API_HOST_PREFIX = "api.";

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if(redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException("Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication");
        }

        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        String host = request.getHeader(HttpHeaders.HOST);
        boolean onApi = host != null && host.startsWith(API_HOST_PREFIX);
        boolean secureProxy = "true".equals(request.getHeader("X-Is-Secure"));
        log.info("authed:{} host:{} onApi:{} secure:{} secureProxy:{} redirectTo:{}",
                 authentication.isAuthenticated(),
                 host,
                 onApi,
                 request.isSecure(),
                 secureProxy,
                 targetUrl);

        String token = tokenProvider.createToken(authentication);
        ResponseCookie.ResponseCookieBuilder cb = ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .path("/")
                .maxAge(appProperties.getAuth().getTokenExpirationMsec() / 1000);
        if (onApi) {
            // Strip the prefix, so it'll be available to the client too.
            cb.domain(host.substring(API_HOST_PREFIX.length()));
        }
        if (request.isSecure() || secureProxy) {
            // If we're on a secure endpoint, set it up for cross-origin use by
            // the import bookmarklet. If isn't secure... bummer?
            cb.secure(true)
                    .sameSite("None");
        }
        CookieUtils.addCookie(response, cb);
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);

        return appProperties.getOauth2().getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    // Only validate host and port. Let the clients use different paths if they want to
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }
}
