package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

@Component
public class PlanItemResolver implements GraphQLResolver<PlanItem> {

    Double getQuantity(PlanItem task) {
        return task.getQuantity().getQuantity();
    }

    String getUnits(PlanItem task) {
        Quantity q = task.getQuantity();
        if (q.hasUnits()) {
            return q.getUnits().getName();
        } else {
            return null;
        }
    }

}
