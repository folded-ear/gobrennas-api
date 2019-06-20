package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/friends")
@PreAuthorize("hasRole('USER')")
public class FriendController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Iterable<User> getFriends(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return userRepository.findByIdNot(userPrincipal.getId());
    }

}
