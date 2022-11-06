package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PlanResolver implements GraphQLResolver<TaskList> {

    public List<AccessControlEntry> getGrants(TaskList taskList) {
        Map<User, AccessLevel> grants = taskList.getAcl().getGrants();

        return grants.entrySet()
                .stream()
                .map((entry) -> new AccessControlEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
