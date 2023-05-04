package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.services.PlanService;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class PlanResolver implements GraphQLResolver<Plan> {

    @Autowired
    private PlanService planService;

    public List<AccessControlEntry> getGrants(Plan plan) {
        return AclHelpers.getGrants(plan);
    }

    public List<PlanItem> descendants(Plan plan) {
        return planService.getTreeById(plan);
    }

}
