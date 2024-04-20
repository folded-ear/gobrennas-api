package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class RefreshPantryItemDuplicates extends QueueProcessor {

    protected RefreshPantryItemDuplicates() {
        super("q_pantry_item_duplicates", 1);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Order(EventHandlerSlot.DUPLICATES)
    public void handleRefresh(PantryItemNeedsDuplicatesFound ignored) {
        // This is COARSE, to say the least...
        enqueueAll();
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

    @Scheduled(cron = "${random.int[60]} 2-59/5 * * * *")
    @SuppressWarnings("ScheduledMethodInspection")
    public int refreshQueued() {
        return drainQueue();
    }

}
