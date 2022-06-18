package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class RecipeSearchRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private UserRepository userRepository;

    private RecipeSearchRepositoryImpl repo;

    @BeforeEach
    public void _createRepo() {
        repo = new RecipeSearchRepositoryImpl();
        repo.setEntityManager(entityManager);
    }

    @BeforeEach
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
        l.stream().map(Recipe::getName).forEach(System.out::println);
        assertEquals("Pizza", l.get(0).getName());
        assertEquals("Pizza Crust", l.get(1).getName());
        assertEquals("Pizza Sauce", l.get(2).getName());
        assertEquals(3, l.size());
    }

    @Test
    public void multipleTerms() {
        List<Recipe> l = repo.searchRecipes("pizza dinner", PageRequest.of(0, 10))
                .getContent();
        l.stream().map(Recipe::getName).forEach(System.out::println);
        assertEquals("Pizza", l.get(0).getName()); // both
        assertEquals("Pizza Crust", l.get(1).getName()); // name
        assertEquals("Pizza Sauce", l.get(2).getName()); // name
        assertEquals("ZZZ Dinner", l.get(3).getName()); // label
        assertEquals("Fried Chicken", l.get(4).getName()); // label
        assertEquals(5, l.size());
    }

    @Test
    public void paging() {
        List<Recipe> l = repo.searchRecipes("pizza", PageRequest.of( 1, 2))
                .getContent();
        l.stream().map(Recipe::getName).forEach(System.out::println);
        assertEquals("Pizza Sauce", l.get(0).getName());
        assertEquals(1, l.size());
    }

    @Test
    public void labelMatch() {
        List<Recipe> l = repo.searchRecipes("dinner", PageRequest.of(0, 10))
                .getContent();
        l.stream().map(Recipe::getName).forEach(System.out::println);
        assertEquals("ZZZ Dinner", l.get(0).getName()); // name match orders before other locations
        assertEquals("Fried Chicken", l.get(1).getName());
        assertEquals("Pizza", l.get(2).getName());
        assertEquals(3, l.size());
    }

    @Test
    public void directionsMatch() {
        List<Recipe> l = repo.searchRecipes("knead", PageRequest.of(0, 10))
                .getContent();
        l.stream().map(Recipe::getName).forEach(System.out::println);
        assertEquals("Pizza Crust", l.get(0).getName());
        assertEquals(1, l.size());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void byOwner() {
        User alice = principalAccess.getUser();
        User bob = userRepository.findByEmail("bob@example.com").get();
        User eve = userRepository.findByEmail("eve@example.com").get();
        List<Recipe> l = repo.searchRecipes("pizza", PageRequest.of(0, 10))
                .getContent();
        assertEquals(3, l.size()); // sanity
        l.get(1).setOwner(bob);
        l.get(2).setOwner(eve);

        l = repo.searchRecipesByOwner(
                Collections.singletonList(alice),
                "pizza",
                PageRequest.of(0, 10)
        ).getContent();

        assertEquals(1, l.size());
        assertEquals("Pizza", l.get(0).getName());

        l = repo.searchRecipesByOwner(
                Arrays.asList(alice, eve),
                "pizza",
                PageRequest.of(0, 10)
        ).getContent();

        assertEquals(2, l.size());
        assertEquals("Pizza", l.get(0).getName());
        assertEquals("Pizza Sauce", l.get(1).getName());

        l = repo.searchRecipesByOwner(
                Arrays.asList(bob, eve),
                "",
                PageRequest.of(0, 10)
        ).getContent();

        assertEquals(2, l.size());
        assertEquals("Pizza Crust", l.get(0).getName());
        assertEquals("Pizza Sauce", l.get(1).getName());
    }

}
