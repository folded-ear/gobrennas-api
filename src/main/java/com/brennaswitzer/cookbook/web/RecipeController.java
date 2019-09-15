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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.Valid;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
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
            @RequestParam(name = "scope", defaultValue = "mine") String scope
    ) {
        List<Recipe> recipes = "everyone".equals(scope)
                ? recipeService.findEveryonesRecipes()
                : recipeService.findMyRecipes();
        return recipes
                .stream()
                .map(IngredientInfo::from)
                .collect(Collectors.toList());
    }

    @PostMapping("")
    @Transactional
    public ResponseEntity<?> createNewRecipe(@Valid @RequestBody IngredientInfo info, BindingResult result) {
        // begin kludge (1 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (1 of 3)
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.createNewRecipe(recipe);
        return new ResponseEntity<>(IngredientInfo.from(recipe1), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateRecipe(@Valid @RequestBody IngredientInfo info, BindingResult result) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // begin kludge (2 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (2 of 3)
        ResponseEntity<?> errors = validationService.validationService(result);
        if(errors != null) return errors;

        Recipe recipe1 = recipeService.updateRecipe(recipe);
        return new ResponseEntity<>(IngredientInfo.from(recipe1), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public IngredientInfo getRecipeById(@PathVariable("id") Long id) {
        Optional<Recipe> recipe = recipeService.findRecipeById(id);
        recipe.orElseThrow(NoResultException::new);
        return IngredientInfo.from(recipe.get());
    }

    // begin kludge (3 of 3)
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
    // end kludge (3 of 3)

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        recipeService.deleteRecipeById(id);

        return new ResponseEntity<String>("Recipe was deleted", HttpStatus.OK);
    }

    @PostMapping("/_actions")
    @ResponseBody
    public Object performGlobalAction(
            @RequestBody RecipeAction action
    ) {
        return action.execute(recipeService);
    }

    @PostMapping("/{id}/_actions")
    @ResponseBody
    public Object performRecipeAction(
            @PathVariable("id") Long id,
            @RequestBody RecipeAction action
    ) {
        return action.execute(id, recipeService);
    }

}
