package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;

public final class UserTestUtils {

    private UserTestUtils() {}

    public static User createUser(String name) {
        return new User(name,
                        name.toLowerCase(),
                        name.toLowerCase() + "@gobrennas.com",
                        "<HIDDEN>");
    }

}
