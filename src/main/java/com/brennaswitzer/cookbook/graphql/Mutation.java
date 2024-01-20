package com.brennaswitzer.cookbook.graphql;

import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Mutation implements GraphQLMutationResolver {

    @Autowired
    public LibraryMutation library;

    @Autowired
    public TimerMutation timer;

    @Autowired
    public FavoriteMutation favorite;

    @Autowired
    public PlannerMutation planner;

}
