package com.brennaswitzer.cookbook.resolvers;

import com.brennaswitzer.cookbook.domain.Plan;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class PlanResolver implements GraphQLResolver<Plan> {

    public List<AccessControlEntry> getGrants(Plan plan) {
        return AclHelpers.getGrants(plan);
    }

}
