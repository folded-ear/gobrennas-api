package com.brennaswitzer.cookbook.services.indexing;

import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@WithAliceBobEve(developer = true)
@Slf4j
class IngredientIndexSmokeTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    @Autowired
    IngredientFulltextIndexer indexer;

    @Autowired
    ReindexIngredients reindex;

    private RecipeBox box;

    @BeforeEach
    void setUp() {
        box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        reindex.reindexQueued();
        log.info("Test setup complete.");
    }

    @Test
    void onShotReindex() {
        indexer.reindexIngredientImmediately(
                new ReindexIngredientEvent(
                        box.flour.getId()));
    }

    @Test
    void reindexAll() {
        reindex.reindexQueued();
        reindex.enqueueAll();
        reindex.reindexQueued();
    }

}
