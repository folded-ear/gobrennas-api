package com.brennaswitzer.cookbook.services.indexing;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class IndexStats {

    /**
     * Number of items on the reindexing queue.
     */
    long queueSize;

    /**
     * Maximum age in seconds of items on the reindexing queue, -1 if the queue is empty.
     */
    long queueMaxAge;

    /**
     * Minimum age in seconds of items on the reindexing queue, -1 if the queue is empty.
     */
    long queueMinAge;

    /**
     * Total number of recipes.
     */
    long recipeCount;

    /**
     * Number of recipes that have been indexed.
     */
    long indexedRecipeCount;

    /**
     * Number of recipes with a stale index.
     */
    long staleRecipeCount;

    /**
     * Number of recipes that have not been indexed.
     */
    @SuppressWarnings("unused") // Jackson uses it
    public long getNonIndexedRecipeCount() {
        return recipeCount - indexedRecipeCount;
    }

}
