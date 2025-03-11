package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.domain.UserDevice;
import com.brennaswitzer.cookbook.domain.UserPreference;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.AssembleUserPreferences;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class UserResolver implements GraphQLResolver<User> {

    @Autowired
    private AssembleUserPreferences assembleUserPreferences;

    public List<String> roles(User user,
                              DataFetchingEnvironment env) {
        UserPrincipal principal = PrincipalUtil.from(env);
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

    public Collection<UserPreference> preferences(User user,
                                                  String deviceKey) {
        return assembleUserPreferences.assemble(user, deviceKey);
    }

    public List<UserDevice> devices(User user) {
        // Don't care about order server-side, but clients usually do. Helpful!
        List<UserDevice> devices = new ArrayList<>(user.getDevices());
        devices.sort(Comparator.comparing(BaseEntity::getId));
        return devices;
    }

}
