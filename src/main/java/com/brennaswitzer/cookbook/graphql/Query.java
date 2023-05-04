package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.AccessControlled;
import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.services.PlanService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused")
@Component
public class Query implements GraphQLQueryResolver {

    @Autowired
    private List<BaseEntityRepository<?>> repositories;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserPrincipalAccess userPrincipalAccess;

    @Autowired
    public TimerQuery timer;

    @Autowired
    public FavoriteQuery favorite;

    @Autowired
    public LibraryQuery library;

    @Autowired
    public LabelsQuery labels;

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

    List<Plan> getPlans() {
        List<Plan> result = new ArrayList<>();
        Iterable<Plan> plans = planService.getPlans();
        Iterator<Plan> iterator = plans.iterator();
        iterator.forEachRemaining(result::add);
        return result;
    }

    PlanItem getPlanItem(Long id) {
        return planService.getTaskById(id);
    }

    User getCurrentUser() {
        return userPrincipalAccess.getUser();
    }

}
