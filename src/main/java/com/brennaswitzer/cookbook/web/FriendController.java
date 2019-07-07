package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.payload.UserInfo;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@RestController
@RequestMapping("api/friends")
@PreAuthorize("hasRole('USER')")
public class FriendController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public Iterable<UserInfo> getFriends(
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return userRepository.findByIdNot(userPrincipal.getId())
                .stream()
                .map(UserInfo::fromUser)
                .collect(Collectors.toList());
    }

}
