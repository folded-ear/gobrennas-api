package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.security.CookieTokenAuthenticationFilter;
import com.brennaswitzer.cookbook.security.CookieTokenLogoutHandler;
import com.brennaswitzer.cookbook.security.CustomUserDetailsService;
import com.brennaswitzer.cookbook.security.HeaderTokenAuthenticationFilter;
import com.brennaswitzer.cookbook.security.RestAuthenticationEntryPoint;
import com.brennaswitzer.cookbook.security.oauth2.CustomOAuth2UserService;
import com.brennaswitzer.cookbook.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.brennaswitzer.cookbook.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.brennaswitzer.cookbook.security.oauth2.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.DisableEncodeUrlFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * OAuth2 Login Flow
 * The OAuth2 login flow will be initiated by the frontend client by sending
 * the user to the endpoint
 * http://localhost:8080/oauth2/authorize/{provider}?redirect_uri=<redirect_uri_after_login>.
 * <p>
 * The provider path parameter is one of google, facebook, or github. The
 * redirect_uri is the URI to which the user will be redirected once the
 * authentication with the OAuth2 provider is successful. This is different from
 * the OAuth2 redirectUri.
 * <p>
 * On receiving the authorization request, Spring Security’s OAuth2 client will
 * redirect the user to the AuthorizationUrl of the supplied provider.
 * <p>
 * All the state related to the authorization request is saved using the
 * authorizationRequestRepository specified in the SecurityConfig.
 * <p>
 * The user now allows/denies permission to your app on the provider’s page. If
 * the user allows permission to the app, the provider will redirect the user to
 * the callback url http://localhost:8080/oauth2/callback/{provider} with an
 * authorization code. If the user denies the permission, he will be redirected
 * to the same callbackUrl but with an error.
 * <p>
 * If the OAuth2 callback results in an error, Spring security will invoke the
 * oAuth2AuthenticationFailureHandler specified in the above SecurityConfig.
 * <p>
 * If the OAuth2 callback is successful and it contains the authorization code,
 * Spring Security will exchange the authorization_code for an access_token and
 * invoke the customOAuth2UserService specified in the above SecurityConfig.
 * <p>
 * The customOAuth2UserService retrieves the details of the authenticated user
 * and creates a new entry in the database or updates the existing entry with
 * the same email.
 * <p>
 * Finally, the oAuth2AuthenticationSuccessHandler is invoked. It creates a JWT
 * authentication token for the user and sends the user to the redirect_uri
 * along with the JWT token in a query string.
 **/

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true
)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private CookieTokenLogoutHandler cookieTokenLogoutHandler;

    @Bean
    public HeaderTokenAuthenticationFilter headerTokenAuthenticationFilter() {
        return new HeaderTokenAuthenticationFilter();
    }

    @Bean
    public CookieTokenAuthenticationFilter cookieTokenAuthenticationFilter() {
        return new CookieTokenAuthenticationFilter();
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.headers(h -> h.frameOptions(fo -> fo.sameOrigin()));
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(c -> c.disable());
        http.formLogin(fl -> fl.disable());
        http.httpBasic(hb -> hb.disable());
        http.exceptionHandling(eh -> eh.authenticationEntryPoint(new RestAuthenticationEntryPoint()));
        http.logout(l -> l.logoutUrl("/oauth2/logout")
                .logoutSuccessUrl(appProperties.getPublicUrl())
                .addLogoutHandler(cookieTokenLogoutHandler)
                .permitAll());
        http.authorizeHttpRequests(r -> r.requestMatchers(
                        "/",
                        "/error",
                        "/favicon.ico",
                        "/favicon.svg",
                        "/shared/*/*",
                        "/*/*.png",
                        "/*/*.gif",
                        "/*/*.svg",
                        "/*/*.jpg",
                        "/*/*.html",
                        "/*/*.css",
                        "/*/*.js"
                ).permitAll()
                .requestMatchers(
                        "/api/**"
                ).authenticated()
                .anyRequest().permitAll());
        http.oauth2Login(l -> l.authorizationEndpoint(
                        ae -> ae.baseUri("/oauth2/authorize")
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository()))
                .redirectionEndpoint(re -> re.baseUri("/oauth2/callback/*"))
                .userInfoEndpoint(uie -> uie.userService(customOAuth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler));

        // Add our custom Token based authentication filters
        http.addFilterBefore(cookieTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(headerTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Give every request a unique thread name.
        http.addFilterBefore(
                new OncePerRequestFilter() {
                    @Override
                    protected void doFilterInternal(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    FilterChain filterChain) throws ServletException, IOException {
                        String original = Thread.currentThread().getName();
                        Thread.currentThread().setName(original
                                                       + '-'
                                                       + System.currentTimeMillis());
                        try {
                            filterChain.doFilter(request, response);
                        } finally {
                            Thread.currentThread().setName(original);
                        }
                    }
                },
                // this is the first filter in Spring Security's chain
                DisableEncodeUrlFilter.class);

        return http.build();
    }

}
