package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import lombok.Getter;
import lombok.Setter;

public class AccessControlEntry {

    @Getter
    @Setter
    User user;

    @Getter
    @Setter
    AccessLevel level;

    public AccessControlEntry() {
    }

    public AccessControlEntry(User user, AccessLevel accessLevel) {
        setUser(user);
        setLevel(accessLevel);
    }

}
