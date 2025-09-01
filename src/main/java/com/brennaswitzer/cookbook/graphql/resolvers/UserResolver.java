package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.AssembleUserPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Controller
public class UserResolver {

    @Autowired
    private AssembleUserPreferences assembleUserPreferences;

    @SchemaMapping
    public List<String> roles(User user,
                              @AuthenticationPrincipal UserPrincipal principal) {
        // if not the current user, create a new instance
        if (!Objects.equals(principal.getId(), user.getId())) {
            principal = UserPrincipal.create(user);
        }
        return principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(it -> it.startsWith("ROLE_"))
                .map(it -> it.substring(5))
                .toList();
    }

    @SchemaMapping
    public Collection<UserPreference> preferences(User user,
                                                  @Argument String deviceKey) {
        return assembleUserPreferences.assemble(user, deviceKey);
    }

}
