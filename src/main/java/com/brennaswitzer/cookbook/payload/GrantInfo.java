package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GrantInfo {

    public static GrantInfo fromGrant(User user, AccessLevel level) {
        return new GrantInfo(user.getId(), level);
    }

    private Long userId;

    private AccessLevel accessLevel;

    public GrantInfo() {}

    public GrantInfo(Long userId, AccessLevel level) {
        setUserId(userId);
        setAccessLevel(level);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

}
