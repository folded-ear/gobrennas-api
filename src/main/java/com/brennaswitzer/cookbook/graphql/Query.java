package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Component
public class Query implements GraphQLQueryResolver {

    @Autowired
    private List<BaseEntityRepository<?>> repositories;

    @Autowired
    private UserPrincipalAccess userPrincipalAccess;

    @Autowired
    public TimerQuery timer;

    @Autowired
    public FavoriteQuery favorite;

    @Autowired
    public PantryQuery pantry;

    @Autowired
    public LibraryQuery library;

    @Autowired
    public LabelsQuery labels;

    @Autowired
    public PlannerQuery planner;

    Object getNode(Long id) {
        return repositories.stream()
                .map(r -> r.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(it -> !(it instanceof AccessControlled) ||
                        ((AccessControlled) it).isPermitted(
                                userPrincipalAccess.getUser(),
                                AccessLevel.VIEW))
                .findFirst()
                .orElse(null);
    }

    User getCurrentUser() {
        return userPrincipalAccess.getUser();
    }

}
