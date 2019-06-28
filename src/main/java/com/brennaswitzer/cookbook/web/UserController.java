package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {
        return userRepository.getById(userPrincipal.getId());
    }
}
