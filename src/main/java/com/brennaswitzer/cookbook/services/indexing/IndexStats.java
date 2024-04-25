package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.payload.QueueStats;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IndexStats {

    QueueStats queueStats;

    /**
     * Total number of ingredients.
     */
    long ingredientCount;

    /**
     * Number of ingredients that have been indexed.
     */
    long indexedIngredientCount;

    /**
     * Number of ingredients with a stale index.
     */
    long staleIngredientCount;

    /**
     * Number of ingredients that have not been indexed.
     */
    @SuppressWarnings("unused") // Jackson uses it
    public long getNonIndexedIngredientCount() {
        return ingredientCount - indexedIngredientCount;
    }

}
