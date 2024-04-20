package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.repositories.impl.PantryItemSearchRequest;
import com.brennaswitzer.cookbook.services.indexing.RefreshPantryItemDuplicates;
import com.brennaswitzer.cookbook.services.indexing.ReindexIngredients;
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

    @Autowired
    private RefreshPantryItemDuplicates refreshDupes;

    @Autowired
    private ReindexIngredients reindex;

    private RecipeBox box;

    @BeforeEach
    void setUp() {
        box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
    }

    private void rebuildDupes() {
        assert 0 != reindex.drainQueue();
        assert 0 != refreshDupes.enqueueAll();
        assert 0 != refreshDupes.drainQueue();
    }

    @Test
    void sortedByName() {
        rebuildDupes();

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by("name"))
                        .build());

        PantryItem salt = extractItemByName(result, "salt");
        PantryItem chicken = extractItemByName(result, "chicken");
        // didn't _have_ to retrieve them, so make sure they weren't
        assertNull(salt.getUseCount());
        assertNull(salt.getDuplicateCount());
        assertNull(chicken.getUseCount());
        assertNull(chicken.getDuplicateCount());
        assertNotNull(repo.countTotalUses(salt));
        assertNotNull(repo.countTotalUses(chicken));
        assertNotNull(repo.countDuplicates(salt));
        assertNotNull(repo.countDuplicates(chicken));
    }

    @Test
    void sortByUseCount() {
        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by(Sort.Direction.DESC, "useCount"))
                        .build());

        PantryItem salt = extractItemByName(result, "salt");
        // use count came through, since it _had_ to be retrieved
        assertEquals(2, salt.getUseCount());
        assertNull(salt.getDuplicateCount());
        assertEquals(2, repo.countTotalUses(salt));
    }

    @Test
    void sortByDuplicateCount() {
        rebuildDupes();

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by(Sort.Direction.DESC, "duplicateCount"))
                        .build());

        PantryItem chicken = extractItemByName(result, "chicken");
        // duplicate count came through, since it _had_ to be retrieved
        assertNull(chicken.getUseCount());
        assertEquals(1, chicken.getDuplicateCount());
        assertEquals(1, repo.countDuplicates(chicken));
    }

    @Test
    void sortByBothLazyCounts() {
        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .sort(Sort.by(Sort.Direction.DESC, "useCount", "duplicateCount"))
                        .build());

        PantryItem salt = extractItemByName(result, "salt");
        // both count came through, since they _had_ to be retrieved
        assertEquals(2, salt.getUseCount());
        assertEquals(0, salt.getDuplicateCount());
        assertEquals(2, repo.countTotalUses(salt));
        assertEquals(0, repo.countDuplicates(salt));
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

    @Test
    void duplicatesOf_none() {
        rebuildDupes();

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .duplicateOf(box.salt.getId())
                        .build());

        assertEquals(0, result.size());
    }

    @Test
    void duplicatesOf_some() {
        rebuildDupes();

        SearchResponse<PantryItem> result = repo.search(
                PantryItemSearchRequest.builder()
                        .duplicateOf(box.chicken.getId())
                        .build());

        assertEquals(1, result.size());
        assertEquals("chicken thigh", result.getContent().iterator().next().getName());
    }

    private PantryItem extractItemByName(SearchResponse<PantryItem> result, String name) {
        return result.getContent()
                .stream()
                .filter(it -> name.equals(it.getName()))
                .findFirst()
                .orElseThrow();
    }

}
