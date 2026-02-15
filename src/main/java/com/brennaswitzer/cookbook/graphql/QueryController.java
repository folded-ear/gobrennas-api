package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.CurrentUser;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class QueryController {

    @Autowired
    private List<BaseEntityRepository<?>> repositories;

    @Autowired
    private UserRepository userRepository;

    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    Object node(@Argument Long id,
                @CurrentUser UserPrincipal userPrincipal) {
        return repositories.stream()
                .map(r -> r.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Hibernate::unproxy)
                .filter(it -> !(it instanceof AccessControlled ac) ||
                              ac.isPermitted(getUser(userPrincipal),
                                             AccessLevel.VIEW))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("There is no node with id: " + id));
    }

    /**
     * @deprecated prefer {@link ProfileQueryController#me}
     */
    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    @Deprecated
    User getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return getUser(userPrincipal);
    }

    private User getUser(UserPrincipal userPrincipal) {
        return userRepository.getReferenceById(userPrincipal.getId());
    }

}
