package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class RecipeSearchRepositoryImplTest {

    @Autowired
    EntityManager entityManager;

    @Autowired
    UserPrincipalAccess principalAccess;

    RecipeSearchRepositoryImpl repo;

    @Before
    public void _createRepo() {
        repo = new RecipeSearchRepositoryImpl();
        repo.setEntityManager(entityManager);
    }

    @Before
    public void _loadBox() {
        RecipeBox box = new RecipeBox();
        User owner = principalAccess.getUser();
        box.persist(entityManager, owner);
        Recipe r = new Recipe("ZZZ Dinner");
        r.setOwner(owner);
        entityManager.persist(r);
    }

    @Test
    public void unbounded() {
        assertEquals(6, repo.searchRecipes(null, PageRequest.of(0, 10)).getNumberOfElements());
        assertEquals(6, repo.searchRecipes("", PageRequest.of(0, 10)).getNumberOfElements());
    }

    @Test
    public void nameMatch() {
        List<Recipe> l = repo.searchRecipes("pizza", PageRequest.of(0, 10))
                .getContent();
        assertEquals(3, l.size());
        assertEquals("Pizza", l.get(0).getName());
        assertEquals("Pizza Crust", l.get(1).getName());
        assertEquals("Pizza Sauce", l.get(2).getName());
    }

    @Test
    public void paging() {
        List<Recipe> l = repo.searchRecipes("pizza", PageRequest.of( 1, 2))
                .getContent();
        assertEquals(1, l.size());
        assertEquals("Pizza Sauce", l.get(0).getName());
    }

    @Test
    public void labelMatch() {
        List<Recipe> l = repo.searchRecipes("dinner", PageRequest.of(0, 10))
                .getContent();
        assertEquals(3, l.size());
        assertEquals("ZZZ Dinner", l.get(0).getName()); // name match orders before other locations
        assertEquals("Fried Chicken", l.get(1).getName());
        assertEquals("Pizza", l.get(2).getName());
    }

    @Test
    public void directionsMatch() {
        List<Recipe> l = repo.searchRecipes("knead", PageRequest.of(0, 10))
                .getContent();
        assertEquals(1, l.size());
        assertEquals("Pizza Crust", l.get(0).getName());
    }

}
