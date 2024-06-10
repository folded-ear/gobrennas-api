package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import graphql.kickstart.tools.GraphQLResolver;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
public class PlannedRecipeHistoryResolver implements GraphQLResolver<PlannedRecipeHistory> {

    public OffsetDateTime plannedAt(PlannedRecipeHistory history) {
        return history.getPlannedAt()
                .atOffset(ZoneOffset.UTC);
    }

    public OffsetDateTime doneAt(PlannedRecipeHistory history) {
        return history.getDoneAt()
                .atOffset(ZoneOffset.UTC);
    }

    public Long ratingInt(PlannedRecipeHistory history) {
        return history.isRated()
                ? history.getRating().getId()
                : null;
    }

}
