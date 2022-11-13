package com.brennaswitzer.cookbook.config;

import com.brennaswitzer.cookbook.security.CookieTokenAuthenticationFilter;
import com.brennaswitzer.cookbook.security.CustomUserDetailsService;
import com.brennaswitzer.cookbook.security.HeaderTokenAuthenticationFilter;
import com.brennaswitzer.cookbook.security.RestAuthenticationEntryPoint;
import com.brennaswitzer.cookbook.security.oauth2.CustomOAuth2UserService;
import com.brennaswitzer.cookbook.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.brennaswitzer.cookbook.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.brennaswitzer.cookbook.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.brennaswitzer.cookbook.security.CookieTokenAuthenticationFilter.TOKEN_COOKIE_NAME;


/**
 * OAuth2 Login Flow
 * The OAuth2 login flow will be initiated by the frontend client by sending the user to the endpoint http://localhost:8080/oauth2/authorize/{provider}?redirect_uri=<redirect_uri_after_login>.
 * <p>
 * The provider path parameter is one of google, facebook, or github. The redirect_uri is the URI to which the user will be redirected once the authentication with the OAuth2 provider is successful. This is different from the OAuth2 redirectUri.
 * <p>
 * On receiving the authorization request, Spring Security’s OAuth2 client will redirect the user to the AuthorizationUrl of the supplied provider.
 * <p>
 * All the state related to the authorization request is saved using the authorizationRequestRepository specified in the SecurityConfig.
 * <p>
 * The user now allows/denies permission to your app on the provider’s page. If the user allows permission to the app, the provider will redirect the user to the callback url http://localhost:8080/oauth2/callback/{provider} with an authorization code. If the user denies the permission, he will be redirected to the same callbackUrl but with an error.
 * <p>
 * If the OAuth2 callback results in an error, Spring security will invoke the oAuth2AuthenticationFailureHandler specified in the above SecurityConfig.
 * <p>
 * If the OAuth2 callback is successful and it contains the authorization code, Spring Security will exchange the authorization_code for an access_token and invoke the customOAuth2UserService specified in the above SecurityConfig.
 * <p>
 * The customOAuth2UserService retrieves the details of the authenticated user and creates a new entry in the database or updates the existing entry with the same email.
 * <p>
 * Finally, the oAuth2AuthenticationSuccessHandler is invoked. It creates a JWT authentication token for the user and sends the user to the redirect_uri along with the JWT token in a query string.
 **/

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
            .cors()
                .and()
            .headers()
                .frameOptions().sameOrigin()
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .csrf()
                .disable()
            .formLogin()
                .disable()
            .httpBasic()
                .disable()
            .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
            .logout()
                .logoutUrl("/oauth2/logout")
                .logoutSuccessUrl(appProperties.getPublicUrl())
                .deleteCookies("JSESSIONID", TOKEN_COOKIE_NAME)
                .permitAll()
                .and()
            .authorizeRequests()
                .antMatchers("/",
                    "/error",
                    "/favicon.ico",
                    "/shared/**/*",
                    "/**/*.png",
                    "/**/*.gif",
                    "/**/*.svg",
                    "/**/*.jpg",
                    "/**/*.html",
                    "/**/*.css",
                    "/**/*.js")
                    .permitAll()
                .antMatchers("/api/**")
                    .authenticated()
                .anyRequest()
                    .permitAll()
                    .and()
                .oauth2Login()
                    .authorizationEndpoint()
                        .baseUri("/oauth2/authorize")
                        .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                        .and()
                    .redirectionEndpoint()
                        .baseUri("/oauth2/callback/*")
                        .and()
                    .userInfoEndpoint()
                        .userService(customOAuth2UserService)
                        .and()
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler);
        //@formatter:on

        // Add our custom Token based authentication filter
        http.addFilterBefore(cookieTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(headerTokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

}
