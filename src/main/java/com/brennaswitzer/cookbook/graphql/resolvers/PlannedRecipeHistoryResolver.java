package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Controller
public class PlannedRecipeHistoryResolver {

    @SchemaMapping
    public OffsetDateTime plannedAt(PlannedRecipeHistory history) {
        return history.getPlannedAt()
                .atOffset(ZoneOffset.UTC);
    }

    @SchemaMapping
    public OffsetDateTime doneAt(PlannedRecipeHistory history) {
        return history.getDoneAt()
                .atOffset(ZoneOffset.UTC);
    }

    @SchemaMapping
    public Long ratingInt(PlannedRecipeHistory history) {
        return history.isRated()
                ? history.getRating().getId()
                : null;
    }

}
