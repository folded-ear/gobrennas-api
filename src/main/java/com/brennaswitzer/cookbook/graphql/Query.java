package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Task;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.services.TaskService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import graphql.kickstart.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class Query implements GraphQLQueryResolver {

    @Autowired
    private List<JpaRepository<? extends Identified, Long>> repositories;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserPrincipalAccess userPrincipalAccess;

    Object getNode(Long id) {
        return repositories.stream()
                .map(r -> r.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
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
