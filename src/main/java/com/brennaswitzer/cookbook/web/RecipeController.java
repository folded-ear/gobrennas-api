package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @PostMapping("")
    public ResponseEntity<Recipe> createNewRecipe(@RequestBody Recipe recipe) {
        Recipe recipe1 = recipeService.saveOrUpdateRecipe(recipe);
        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
    }
}
