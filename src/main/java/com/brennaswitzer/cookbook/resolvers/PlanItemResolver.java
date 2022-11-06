package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Task;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

@Component
public class PlanItemResolver implements GraphQLResolver<Task> {

    Double getQuantity(Task task) {
        return task.getQuantity().getQuantity();
    }

    String getUnits(Task task) {
        Quantity q = task.getQuantity();
        if(q.hasUnits()) {
            return q.getUnits().getName();
        } else return null;
    }

}
