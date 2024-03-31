package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
