package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithAliceBobEve
class PantryItemRepositoryTest {

    @Autowired
    private PantryItemRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @BeforeEach
    void setUp() {
        new RecipeBox().persist(entityManager, principalAccess.getUser());
    }

    @Test
    void nameContainingMatchesSubstrings() {
        assertEquals(List.of("salt"),
                     repo.findAllByNameIgnoreCaseContainingOrderById("ALT")
                             .stream()
                             .map(PantryItem::getName)
                             .toList());
    }

    @Test
    void nameContainingLikeMetacharactersAreLiteral() {
        for (var name : List.of("s_lt", "s%t")) {
            assertEquals(List.of(),
                         repo.findAllByNameIgnoreCaseContainingOrderById(name),
                         name);
        }
    }

}
