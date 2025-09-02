package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessControlEntry {

    private User user;

    private AccessLevel level;

    public AccessControlEntry() {
    }

    public AccessControlEntry(User user, AccessLevel accessLevel) {
        setUser(user);
        setLevel(accessLevel);
    }

}
