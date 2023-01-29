package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class UserPrincipalAccessImpl implements UserPrincipalAccess {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserPrincipal getUserPrincipal() {
        val auth = SecurityContextHolder
            .getContext()
            .getAuthentication();
        if (!auth.isAuthenticated() || !auth.getAuthorities().contains(UserPrincipal.ROLE_USER)) {
            throw new NoUserPrincipalException("No user principal found. Are you logged in?");
        }
        return (UserPrincipal) auth.getPrincipal();
    }

    @Override
    public User getUser() {
        return userRepo.getById(getId());
    }

}
