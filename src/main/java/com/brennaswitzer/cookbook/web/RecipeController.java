package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.RecipeAction;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.LabelService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.services.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/")
    public Iterable<IngredientInfo> getRecipes(
            @RequestParam(name = "scope", defaultValue = "mine") String scope,
            @RequestParam(name = "filter", defaultValue = "") String filter
    ) {
        filter = filter.trim();
        boolean hasFilter = filter.length() > 0;
        List<Recipe> recipes;
        if ("everyone".equals(scope)) {
             recipes = hasFilter
                ? IngredientInfo.fromRecipes(recipeService.findRecipeByName(filter.toLowerCase()))
                : recipeService.findEveryonesRecipes();
        } else {
             recipes =  hasFilter
                ? IngredientInfo.fromRecipes(recipeService.findRecipeByNameAndOwner(filter.toLowerCase()))
                : IngredientInfo.fromRecipes(recipeService.findMyRecipes());
        }

        return recipes
                .stream()
                .map(this::getRecipeInfo)
                .collect(Collectors.toList());
    }

    @PostMapping(value="", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> createNewRecipe(@RequestParam("info") String r, @RequestParam(required = false) MultipartFile photo) throws IOException {

        IngredientInfo info = mapToInfo(r);

        // begin kludge (1 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (1 of 3)

        if (info.isCookThis()) {
            recipe.getIngredients().forEach(itemService::autoRecognize);
        }

        Recipe recipe1 = recipeService.createNewRecipe(recipe);
        if (photo != null) {
            setPhoto(photo, recipe1);
        }

        labelService.updateLabels(recipe1, info.getLabels());

        return new ResponseEntity<>(getRecipeInfo(recipe1), HttpStatus.CREATED);
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @PutMapping(value="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> updateRecipe(@RequestParam("info") String r, @RequestParam(required = false) MultipartFile photo) throws IOException {

        IngredientInfo info = mapToInfo(r);

        // begin kludge (2 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (2 of 3)

        if (photo != null) {
            setPhoto(photo, recipe);
        }

        Recipe recipe1 = recipeService.updateRecipe(recipe);
        labelService.updateLabels(recipe1, info.getLabels());

        return new ResponseEntity<>(getRecipeInfo(recipe1), HttpStatus.OK);
    }

    @PutMapping(value="/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @ResponseBody
    public IngredientInfo setRecipePhoto(@PathVariable("id") Long id, @RequestParam MultipartFile photo) throws IOException {
        //noinspection OptionalGetWithoutIsPresent
        Recipe recipe = recipeService.findRecipeById(id).get();
        setPhoto(photo, recipe);
        return getRecipeInfo(recipeService.updateRecipe(recipe));
    }

    @GetMapping("/{id}")
    public IngredientInfo getRecipeById(@PathVariable("id") Long id) {
        Recipe recipe = getRecipe(id);
        return getRecipeInfo(recipe);
    }

    // begin kludge (3 of 3)
    @Autowired private EntityManager em;
    @GetMapping("/bulk-ingredients/{ids}")
    @SneakyThrows
    public Collection<IngredientInfo> getIngredientsInBulk(
            @PathVariable("ids") Collection<Long> ids
    ) {
        List<IngredientInfo> infos = new ArrayList<>(ids.size());
        for (Long id : ids) {
            infos.add(getIngredientById(id));
        }
        return infos;
    }

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
        IngredientInfo info;

        if (i instanceof Recipe) {
            info = getRecipeInfo((Recipe) i);
        } else {
            info = (IngredientInfo) IngredientInfo.class
                    .getMethod("from", i.getClass())
                    .invoke(null, i);
        }

        return info;
    }
    // end kludge (3 of 3)

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
        removePhoto(getRecipe(id));
        recipeService.deleteRecipeById(id);
        return new ResponseEntity<>("Recipe was deleted", HttpStatus.OK);
    }

    @PostMapping("/{id}/labels")
    @Transactional
    public ResponseEntity<?> addLabel(@PathVariable Long id, @RequestBody String label) {
        Recipe recipe = getRecipe(id);
        labelService.addLabel(recipe, label);
        return new ResponseEntity<>(label, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/labels/{label}")
    @Transactional
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeLable(
            @PathVariable Long id,
            @PathVariable String label
    ) {
        Recipe recipe = getRecipe(id);
        labelService.removeLabel(recipe, label);
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

    private Recipe getRecipe(Long id) {
        Optional<Recipe> recipe = recipeService.findRecipeById(id);
        recipe.orElseThrow(NoResultException::new);
        return recipe.get();
    }

    private IngredientInfo getRecipeInfo(Recipe r) {
        IngredientInfo info = IngredientInfo.from(r);
        if(r.hasPhoto()) {
            info.setPhoto(storageService.load(r.getPhoto().getObjectKey()));
        }
        return info;
    }

    private IngredientInfo mapToInfo(String recipeData) throws IOException {
        return objectMapper.readValue(recipeData, IngredientInfo.class);
    }

    private void setPhoto(MultipartFile photo, Recipe recipe) throws IOException {
        removePhoto(recipe);
        String name = photo.getOriginalFilename();
        if (name == null) {
            name = "photo";
        } else {
            name = S3File.sanitizeFilename(name);
        }
        String objectKey = "recipe/" + recipe.getId() + "/" + name;
        recipe.setPhoto(new S3File(
                storageService.store(photo, objectKey),
                photo.getContentType(),
                photo.getSize()
        ));
    }

    private void removePhoto(Recipe recipe) {
        if (recipe.hasPhoto()) {
            try {
                storageService.remove(recipe.getPhoto().getObjectKey());
            } catch (IOException ioe) {
                throw new RuntimeException("Failed to remove photo", ioe);
            }
            recipe.clearPhoto();
        }
    }

}
