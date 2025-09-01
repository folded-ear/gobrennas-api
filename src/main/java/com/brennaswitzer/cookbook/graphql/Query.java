package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
public class Query {

    @Autowired
    private List<BaseEntityRepository<?>> repositories;

    @Autowired
    private FavoriteQuery favorite;


    @Autowired
    private PantryQuery pantry;


    @Autowired
    private LibraryQuery library;


    @Autowired
    private LabelsQuery labels;


    @Autowired
    private PlannerQuery planner;


    @Autowired
    private ProfileQuery profile;


    @Autowired
    private TextractQuery textract;

    @QueryMapping
    public FavoriteQuery favorite() {
        return favorite;
    }

    @QueryMapping
    public PantryQuery pantry() {
        return pantry;
    }

    @QueryMapping
    public LibraryQuery library() {
        return library;
    }

    @QueryMapping
    public LabelsQuery labels() {
        return labels;
    }

    @QueryMapping
    public PlannerQuery planner() {
        return planner;
    }

    @QueryMapping
    public ProfileQuery profile() {
        return profile;
    }

    @QueryMapping
    public TextractQuery textract() {
        return textract;
    }

    @QueryMapping
    public Object node(@Argument Long id,
                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return repositories.stream()
                .map(r -> r.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Hibernate::unproxy)
                .filter(it -> !(it instanceof AccessControlled ac) ||
                              ac.isPermitted(profile.me(userPrincipal),
                                             AccessLevel.VIEW))
                .findFirst()
                .orElse(null);
    }

    /**
     * @deprecated prefer {@link ProfileQuery#me}
     */
    @QueryMapping
    @Deprecated
    public User getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return profile.me(userPrincipal);
    }

}
