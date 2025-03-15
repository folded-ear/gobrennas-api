package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.Acl;

import java.util.List;

class AclHelpers {

    static List<AccessControlEntry> getGrants(AccessControlled obj) {
        return getGrants(obj.getAcl());
    }

    static List<AccessControlEntry> getGrants(Acl acl) {
        return acl.getGrants()
                .entrySet()
                .stream()
                .map(entry -> new AccessControlEntry(entry.getKey(), entry.getValue()))
                .toList();
    }

}
