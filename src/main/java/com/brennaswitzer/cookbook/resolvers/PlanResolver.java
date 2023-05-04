package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.brennaswitzer.cookbook.util.CollectionUtils.tail;

@SuppressWarnings("unused") // component-scanned for graphql-java
@Component
public class PlanResolver implements GraphQLResolver<Plan> {

    @Autowired
    private PlanService planService;

    public List<AccessControlEntry> grants(Plan plan) {
        return AclHelpers.getGrants(plan);
    }

    public List<PlanItem> children(PlanItem item) {
        return item.getOrderedChildView();
    }

    public List<PlanItem> descendants(Plan plan) {
        return tail(planService.getTreeById(plan));
    }

}
