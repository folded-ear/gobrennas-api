package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@WithAliceBobEve
public class RecipeServiceTest {

    @Autowired
    private RecipeService service;

    @Test
    public void findRecipeByNameTest() {
        Recipe recipe = new Recipe("chicken things");
        service.createNewRecipe(recipe);
        Iterable<Recipe> recipes = service.findRecipeByName("chicken");
        System.out.println(recipes);
    }
}
