package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithAliceBobEve
class PantryItemSearchRepositoryImplTest {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private PantryItemSearchRepositoryImpl repo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Test
    void sortedByName() {
        val box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by("name"))
                        .limit(10)
                        .build());

        // didn't _have_ to retrieve it, so make sure we didn't
        assertNull(result.getContent().iterator().next().getUseCount());
    }

    @Test
    void sortByUseCounts() {
        val box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by("useCount"))
                        .limit(10)
                        .build());

        // pass the use count through, since we _have_ to retrieve it
        assertNotNull(result.getContent().iterator().next().getUseCount());
    }

}
