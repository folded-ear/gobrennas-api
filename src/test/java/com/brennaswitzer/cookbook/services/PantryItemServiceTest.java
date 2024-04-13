package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WithAliceBobEve
class PantryItemServiceTest {

    @Autowired
    private PantryItemService service;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    @Test
    void orderForStore() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        PantryItem flour = box.flour,
                salt = box.salt,
                yeast = box.yeast;
        assertEquals(0, flour.getStoreOrder());
        assertEquals(0, salt.getStoreOrder());
        assertEquals(0, yeast.getStoreOrder());

        // move salt to flour, before
        service.orderForStore(salt.getId(),
                              flour.getId(),
                              false);

        assertEquals(2, flour.getStoreOrder());
        assertEquals(1, salt.getStoreOrder());
        assertEquals(0, yeast.getStoreOrder());

        // move flour to yeast, after
        service.orderForStore(flour.getId(),
                              yeast.getId(),
                              true);

        assertEquals(2, flour.getStoreOrder());
        assertEquals(3, salt.getStoreOrder());
        assertEquals(1, yeast.getStoreOrder());
    }

    @Test
    void combineItemsRequiresDeveloper() {
        assertThrows(AccessDeniedException.class,
                     () -> service.combineItems(List.of(1L, 2L, 3L)));
    }

}
