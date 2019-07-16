package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.RecipeAction;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/")
    public Iterable<Recipe> getRecipes() {
        return recipeService.findAllRecipes();
    }

    @PostMapping("")
    public ResponseEntity<?> createNewRecipe(@Valid @RequestBody Recipe recipe, BindingResult result) {
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.saveOrUpdateRecipe(recipe);
        return new ResponseEntity<>(recipe1, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecipe(@Valid @RequestBody Recipe recipe, BindingResult result) {
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.saveOrUpdateRecipe(recipe);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.findRecipeById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipeById(id);

        return new ResponseEntity<String>("Recipe was deleted", HttpStatus.OK);
    }

    @PostMapping("/{id}/_actions")
    public void performAction(
            @PathVariable("id") Long id,
            @RequestBody RecipeAction action
    ) {
        action.execute(id, recipeService);
    }

}
