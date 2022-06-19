package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Acl;
import com.brennaswitzer.cookbook.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AclInfo {

    public static AclInfo fromAcl(Acl acl) {
        AclInfo info = new AclInfo();
        User owner = acl.getOwner();
        if (owner != null) {
            info.ownerId = owner.getId();
        }
        Map<Long, AccessLevel> grants = new HashMap<>();
        acl.getGrantedUsers().forEach(u ->
                grants.put(u.getId(), acl.getGrant(u)));
        info.setGrants(grants);
        return info;
    }

    private Long ownerId;

    private Map<Long, AccessLevel> grants;

}
