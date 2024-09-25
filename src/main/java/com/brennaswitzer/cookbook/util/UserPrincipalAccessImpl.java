package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Component
public class UserPrincipalAccessImpl implements UserPrincipalAccess {

    @Autowired
    private UserRepository userRepo;

    @Override
    public Optional<UserPrincipal> findUserPrincipal() {
        return Optional.ofNullable(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(auth -> auth.getAuthorities()
                        .contains(UserPrincipal.ROLE_USER))
                .map(Authentication::getPrincipal)
                .map(UserPrincipal.class::cast);
    }

    @Override
    public User getUser(UserPrincipal principal) {
        return userRepo.getReferenceById(principal.getId());
    }

}
