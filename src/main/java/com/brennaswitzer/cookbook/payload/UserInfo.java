package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.AuthProvider;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserInfo {

    private Long id;
    private String name;
    private String email;
    private String imageUrl;
    private AuthProvider provider;

    private Collection<String> roles;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
    }

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public static UserInfo fromUser(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setEmail(user.getEmail());
        info.setImageUrl(user.getImageUrl());
        info.setName(user.getName());
        info.setProvider(user.getProvider());
        return info;
    }

    public UserInfo withPrincipal(UserPrincipal principal) {
        if (getId() == null) {
            setId(principal.getId());
            setEmail(principal.getEmail());
        } else if (!getId().equals(principal.getId())) {
            throw new IllegalArgumentException("withPrincipal requires the UserInfo's existing User's UserPrincipal");
        }
        setRoles(principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(it -> it.startsWith("ROLE_"))
                .map(it -> it.substring(5))
                .collect(Collectors.toList()));
        return this;
    }
}
