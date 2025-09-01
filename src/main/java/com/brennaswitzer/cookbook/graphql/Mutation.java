package com.brennaswitzer.cookbook.graphql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class Mutation {

    @Autowired
    private PantryMutation pantry;

    @Autowired
    private LibraryMutation library;

    @Autowired
    private FavoriteMutation favorite;

    @Autowired
    private PlannerMutation planner;

    @Autowired
    private TextractMutation textract;

    @Autowired
    private ProfileMutation profile;

    @MutationMapping
    public PantryMutation pantry() {
        return pantry;
    }

    @MutationMapping
    public LibraryMutation library() {
        return library;
    }

    @MutationMapping
    public FavoriteMutation favorite() {
        return favorite;
    }

    @MutationMapping
    public PlannerMutation planner() {
        return planner;
    }

    @MutationMapping
    public TextractMutation textract() {
        return textract;
    }

    @MutationMapping
    public ProfileMutation profile() {
        return profile;
    }

}
