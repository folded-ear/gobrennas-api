package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

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
    void combineItems() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());
        PantryItem flour = box.flour,
                water = box.water;

        var salt = service.combineItems(List.of(box.salt.getId(),
                                                box.flour.getId(),
                                                box.water.getId()));
        entityManager.flush();
        entityManager.clear();

        // flour no longer exists
        assertThrows(EntityNotFoundException.class,
                     () -> ref(flour.getId()).getName());
        // water no longer exists
        assertThrows(EntityNotFoundException.class,
                     () -> ref(water.getId()).getName());
        // pizza crust now references salt three times
        assertEquals(3, entityManager.find(Recipe.class, box.pizzaCrust.getId())
                .getIngredients()
                .stream()
                .filter(r -> r.hasIngredient() && r.getIngredient().equals(salt))
                .count());
        // salt has labels bulk, make ahead
        assertEquals(Set.of(box.bulk, box.makeAhead),
                     salt.getLabels());
        // salt has synonyms nacl, water, h2o, flour
        assertEquals(Set.of("nacl", "water", "h2o", "flour"),
                     salt.getSynonyms());
    }

    private PantryItem ref(Long id) {
        return entityManager.getReference(PantryItem.class, id);
    }

}
