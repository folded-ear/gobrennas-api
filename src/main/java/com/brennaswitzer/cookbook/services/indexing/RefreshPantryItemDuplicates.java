package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RefreshPantryItemDuplicates extends QueueProcessor {

    protected RefreshPantryItemDuplicates() {
        super("q_pantry_item_duplicates", 1);
    }

    @Override
    protected NamedParameterQuery selectAllIds() {
        return new NamedParameterQuery(
                """
                SELECT id
                FROM ingredient
                where dtype = 'PantryItem'
                """);
    }

    @Scheduled(cron = "0 5-59/10 * * * *")
    @SuppressWarnings("ScheduledMethodInspection")
    public int refreshQueued() {
        return drainQueue();
    }

}
