package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ValidationService validationService;

    @PostMapping("")
    public ResponseEntity<?> createNewRecipe(@Valid @RequestBody Recipe recipe, BindingResult result) {
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.saveOrUpdateRecipe(recipe);
        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
    }
}
