package com.brennaswitzer.cookbook.graphql.model;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import lombok.Value;

@Value
public class AccessControlEntry {

    User user;
    AccessLevel level;

}
