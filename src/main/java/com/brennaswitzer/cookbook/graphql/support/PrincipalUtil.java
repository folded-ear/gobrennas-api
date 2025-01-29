package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import graphql.schema.DataFetchingEnvironment;

import java.util.Optional;

public class PrincipalUtil {

    public static void ensurePrincipal(DataFetchingEnvironment env) {
        from(env);
    }

    public static UserPrincipal from(DataFetchingEnvironment env) {
        return optionally(env)
                .orElseThrow(NoUserPrincipalException::new);
    }

    public static Optional<UserPrincipal> optionally(DataFetchingEnvironment env) {
        return env.getGraphQlContext()
                .getOrEmpty(UserPrincipal.class);
    }

}
