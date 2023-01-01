package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.AccessControlled;

import java.util.List;
import java.util.stream.Collectors;

class AclHelpers {

    static List<AccessControlEntry> getGrants(AccessControlled obj) {
        return obj.getAcl()
                .getGrants()
                .entrySet()
                .stream()
                .map(entry -> new AccessControlEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

}
