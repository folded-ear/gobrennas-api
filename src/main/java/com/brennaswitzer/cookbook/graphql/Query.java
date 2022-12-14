package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.repositories.BaseEntityRepository;
import com.brennaswitzer.cookbook.services.TaskService;
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
    private TaskService taskService;

    @Autowired
    private UserPrincipalAccess userPrincipalAccess;

    @Autowired
    public TimerQuery timer;

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

    List<TaskList> getPlans() {
        List<TaskList> result = new ArrayList<>();
        Iterable<TaskList> plans = taskService.getTaskLists();
        Iterator<TaskList> iterator = plans.iterator();
        iterator.forEachRemaining(result::add);
        return result;
    }

    Task getPlanItem(Long id) {
        return taskService.getTaskById(id);
    }

    User getCurrentUser() {
        return userPrincipalAccess.getUser();
    }

}
