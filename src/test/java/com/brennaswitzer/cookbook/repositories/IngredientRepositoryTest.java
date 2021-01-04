package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
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
