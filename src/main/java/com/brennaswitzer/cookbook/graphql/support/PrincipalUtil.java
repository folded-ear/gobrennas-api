package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.util.NoUserPrincipalException;
import graphql.schema.DataFetchingEnvironment;

public class PrincipalUtil {

    public static UserPrincipal from(DataFetchingEnvironment env) {
        return env.getGraphQlContext()
                .<UserPrincipal>getOrEmpty(UserPrincipal.class)
                .orElseThrow(NoUserPrincipalException::new);
    }

}
