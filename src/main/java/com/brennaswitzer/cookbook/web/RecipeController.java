package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.RecipeAction;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.services.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private ValidationService validationService;

    @GetMapping("/")
    public Iterable<IngredientInfo> getRecipes(
    ) {
        return recipeService.findAllRecipes()
                .stream()
                .map(IngredientInfo::from)
                .collect(Collectors.toList());
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
    public IngredientInfo getRecipeById(@PathVariable("id") Long id) {
        Optional<Recipe> recipe = recipeService.findRecipeById(id);
        recipe.orElseThrow(NoResultException::new);
        return IngredientInfo.from(recipe.get());
    }

    // begin kludge
    @Autowired private EntityManager em;
    @SuppressWarnings("JavaReflectionMemberAccess")
    @GetMapping("/or-ingredient/{id}")
    public IngredientInfo getIngredientById(@PathVariable("id") Long id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Ingredient i = em.find(Ingredient.class, id);

        // dynamic dispatch sure would be nice!
//        IngredientInfo.from(i);

        // a Visitor is a tad heavy for a throwaway kludge...

        // Java doesn't let you switch except on primitives...
//        switch (i.getClass().getSimpleName()) {
//            case "PantryItem":
//                return IngredientInfo.from((PantryItem) i);
//            case "Recipe":
//                return IngredientInfo.from((Recipe) i);
//            default:
//                throw new IllegalArgumentException("Can't deal with " + i.getClass().getSimpleName() + ". Yet!");
//        }

        // just reflect it. Screw. That. Poop.
        return (IngredientInfo) IngredientInfo.class
                .getMethod("from", i.getClass())
                .invoke(null, i);
    }
    // end kludge

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
