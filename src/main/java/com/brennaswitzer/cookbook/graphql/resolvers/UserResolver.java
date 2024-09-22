package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class UserResolver implements GraphQLResolver<User> {

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

}
