package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Recipe;
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

    @PostMapping("")
    public ResponseEntity<?> createNewRecipe(@Valid @RequestBody Recipe recipe, BindingResult result) {
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.saveOrUpdateRecipe(recipe);
        return new ResponseEntity<>(recipe, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRecipeById(@PathVariable Long id) {
        Optional<Recipe> recipe = recipeService.findRecipeById(id);
        return new ResponseEntity<>(recipe, HttpStatus.OK);
    }

    @GetMapping("/all")
    public Iterable<Recipe> getAllRecipes() {
        return recipeService.findAllRecipes();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipeById(id);

        return new ResponseEntity<String>("Recipe was deleted", HttpStatus.OK);
    }

    @PostMapping("/{id}/raw-ingredients/to-tasks/{listId}")
    public void doit(
            @PathVariable("id") Long id,
            @PathVariable("listId") Long listId
    ) {
        recipeService.addRawIngredientsToList(id, listId, true);
    }

}
