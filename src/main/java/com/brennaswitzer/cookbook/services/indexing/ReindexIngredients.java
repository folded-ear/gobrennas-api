package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReindexIngredients extends QueueProcessor {

    @Autowired
    private RefreshPantryItemDuplicates refreshPantryItemDuplicates;

    protected ReindexIngredients() {
        super("q_ingredient_fulltext", 5);
    }

    @Override
    protected NamedParameterQuery selectAllIds() {
        return new NamedParameterQuery(
                """
                SELECT id
                FROM ingredient
                """);
    }

    @Scheduled(cron = "0 */10 * * * *")
    @SuppressWarnings("ScheduledMethodInspection")
    public int reindexQueued() {
        int totalRows = drainQueue();
        if (totalRows > 0) {
            refreshPantryItemDuplicates.enqueueAll();
        }
        return totalRows;
    }

}
