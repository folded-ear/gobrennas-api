package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Permission;
import com.brennaswitzer.cookbook.domain.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class GrantInfo {

    public static GrantInfo fromGrant(User user, Permission perm) {
        return new GrantInfo(user.getId(), perm);
    }

    private Long userId;

    private Permission permission;

    public GrantInfo() {}

    public GrantInfo(Long userId, Permission perm) {
        setUserId(userId);
        setPermission(perm);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

}
