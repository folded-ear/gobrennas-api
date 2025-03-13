package com.brennaswitzer.cookbook.security;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements OAuth2User, UserDetails, Identified {

    public static final GrantedAuthority ROLE_USER = new SimpleGrantedAuthority("ROLE_USER");
    public static final GrantedAuthority ROLE_DEVELOPER = new SimpleGrantedAuthority("ROLE_DEVELOPER");

    // super-kludgey authorization "framework"
    private static final String BARNEYS_EMAIL = "bboisvert@gmail.com";
    private static final String BRENNAS_EMAIL = "brenna.switzer@gmail.com";

    private static boolean isDeveloper(User user) {
        return BARNEYS_EMAIL.equalsIgnoreCase(user.getEmail())
            || BRENNAS_EMAIL.equalsIgnoreCase(user.getEmail());
    }
    // end "framework"

    @Getter
    private final Long id;
    @Getter
    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>(2);
        authorities.add(ROLE_USER);
        if (isDeveloper(user)) {
            authorities.add(ROLE_DEVELOPER);
        }
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                authorities
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @SuppressWarnings("WeakerAccess")
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
