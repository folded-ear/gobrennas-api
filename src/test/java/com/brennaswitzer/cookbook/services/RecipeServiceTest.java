package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
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
