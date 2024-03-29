package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@WithAliceBobEve
public class IngredientRepositoryTest {

    @Autowired
    IngredientRepository repo;

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    @Test
    public void nameContainsPrefixMatching() {
        RecipeBox box = new RecipeBox();
        box.persist(entityManager, principalAccess.getUser());

        Iterator<Ingredient> itr = repo.findByNameContainsIgnoreCaseOrderByNameIgnoreCaseAscIdAsc("f")
                .iterator();
        assertEquals("flour", itr.next().getName());
        assertEquals("fresh tomatoes", itr.next().getName());
        assertEquals("Fried Chicken", itr.next().getName());
        assertFalse(itr.hasNext());

        itr = repo.findByNameContainsIgnoreCaseOrderByNameIgnoreCaseAscIdAsc("fr")
                .iterator();
        assertEquals("fresh tomatoes", itr.next().getName());
        assertEquals("Fried Chicken", itr.next().getName());
        assertFalse(itr.hasNext());

        itr = repo.findByNameContainsIgnoreCaseOrderByNameIgnoreCaseAscIdAsc("ea")
                .iterator();
        assertEquals("italian seasoning", itr.next().getName());
        assertEquals("yeast", itr.next().getName());
        assertFalse(itr.hasNext());

        itr = repo.findByNameContainsIgnoreCaseOrderByNameIgnoreCaseAscIdAsc("cru")
                .iterator();
        assertEquals("Pizza Crust", itr.next().getName());
        assertFalse(itr.hasNext());
    }

}
