package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.graphql.model.AccessControlEntry;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class AclResolver {

    @SchemaMapping
    public List<AccessControlEntry> grants(Acl acl) {
        return AclHelpers.getGrants(acl);
    }

}
