package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.TaskList;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class PlanResolver implements GraphQLResolver<TaskList> {

    public List<AccessControlEntry> getGrants(TaskList taskList) {
        return AclHelpers.getGrants(taskList);
    }
}
