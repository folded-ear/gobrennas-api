package com.brennaswitzer.cookbook.graphql;

import graphql.kickstart.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class Mutation implements GraphQLMutationResolver {

    @Autowired
    public PantryMutation pantry;

    @Autowired
    public LibraryMutation library;

    @Autowired
    public FavoriteMutation favorite;

    @Autowired
    public PlannerMutation planner;

    @Autowired
    public TextractMutation textract;

    @Autowired
    public ProfileMutation profile;

}
