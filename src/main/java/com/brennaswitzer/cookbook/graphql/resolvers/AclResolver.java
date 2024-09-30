package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Acl;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AclResolver implements GraphQLResolver<Acl> {

    public List<AccessControlEntry> grants(Acl acl) {
        return AclHelpers.getGrants(acl);
    }

}
