package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import graphql.kickstart.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import org.hibernate.Hibernate;
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
    private UserRepository userRepo;

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

    Object getNode(Long id,
                   DataFetchingEnvironment env) {
        return repositories.stream()
                .map(r -> r.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Hibernate::unproxy)
                .filter(it -> !(it instanceof AccessControlled ac) ||
                              ac.isPermitted(getCurrentUser(env),
                                             AccessLevel.VIEW))
                .findFirst()
                .orElse(null);
    }

    User getCurrentUser(DataFetchingEnvironment env) {
        return userRepo.getById(PrincipalUtil.from(env).getId());
    }

}
