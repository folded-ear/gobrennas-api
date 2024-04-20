package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @BeforeEach
    void setUp() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
    }

    @Test
    void sortedByName() {
        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by("name"))
                        .build());

        // didn't _have_ to retrieve it, so make sure we didn't
        assertNull(result.getContent().iterator().next().getUseCount());
    }

    @Test
    void sortByUseCounts() {
        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by("useCount"))
                        .build());

        // pass the use count through, since we _have_ to retrieve it
        assertNotNull(result.getContent().iterator().next().getUseCount());
    }

    @Test
    void filter() {
        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .filter("bulk")
                        .build());

        assertEquals(List.of("flour", "salt"),
                     result.getContent()
                             .stream()
                             .map(PantryItem::getName)
                             .toList());
    }

}
