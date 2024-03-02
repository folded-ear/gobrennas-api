package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.mapper.IngredientMapper;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.Page;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.LabelService;
import com.brennaswitzer.cookbook.services.RecipeService;
import com.brennaswitzer.cookbook.services.StorageService;
import com.brennaswitzer.cookbook.services.indexing.IndexStats;
import com.brennaswitzer.cookbook.services.indexing.RecipeReindexQueueService;
import com.brennaswitzer.cookbook.util.ShareHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("api/recipe")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @Autowired
    private RecipeReindexQueueService recipeReindexQueueService;

    @Autowired
    private LabelService labelService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ShareHelper shareHelper;

    @Autowired
    private IngredientMapper ingredientMapper;

    @GetMapping("/")
    public Page<IngredientInfo> getRecipes(
            @RequestParam(name = "scope", defaultValue = "mine") String scope,
            @RequestParam(name = "filter", defaultValue = "") String filter,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "99999") int pageSize
    ) {
        SearchResponse<IngredientInfo> response = recipeService.searchRecipes(
                        scope,
                        filter,
                        page * pageSize,
                        pageSize)
                .map(ingredientMapper::recipeToInfo);
        return new Page<>(
                page,
                pageSize,
                response.isFirst(),
                response.isLast(),
                response.getContent());
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> createNewRecipe(@RequestParam("info") String r, @RequestParam(required = false) MultipartFile photo) throws IOException {

        IngredientInfo info = mapToInfo(r);

        // begin kludge (1 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (1 of 3)

        if (info.isCookThis()) {
            recipe.getIngredients().forEach(itemService::autoRecognize);
        }

        Recipe recipe1 = recipeService.createNewRecipe(recipe, Upload.of(photo));

        labelService.updateLabels(recipe1, info.getLabels());

        return new ResponseEntity<>(ingredientMapper.recipeToInfo(recipe1), HttpStatus.CREATED);
    }

    @SuppressWarnings("MVCPathVariableInspection")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<?> updateRecipe(@RequestParam("info") String r, @RequestParam(required = false) MultipartFile photo) throws IOException {

        IngredientInfo info = mapToInfo(r);

        // begin kludge (2 of 3)
        Recipe recipe = info.asRecipe(em);
        // end kludge (2 of 3)

        Recipe recipe1 = recipeService.updateRecipe(recipe, Upload.of(photo));
        labelService.updateLabels(recipe1, info.getLabels());

        return new ResponseEntity<>(ingredientMapper.recipeToInfo(recipe1), HttpStatus.OK);
    }

    @PutMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @ResponseBody
    public IngredientInfo setRecipePhoto(@PathVariable("id") Long id, @RequestParam MultipartFile photo) throws IOException {
        Recipe recipe = recipeService.setRecipePhoto(id, Upload.of(photo));
        return ingredientMapper.recipeToInfo(recipeService.updateRecipe(recipe));
    }

    @GetMapping("/{id}")
    public IngredientInfo getRecipeById(@PathVariable("id") Long id) {
        Recipe recipe = getRecipe(id);
        return ingredientMapper.recipeToInfo(recipe);
    }

    @GetMapping({ "/{id}/share", "/share/{id}" })
    public Object getShareInfoById(
            @PathVariable("id") Long id
    ) {
        //noinspection OptionalGetWithoutIsPresent
        return shareHelper.getInfo(Recipe.class,
                                   recipeService.findRecipeById(id).get());
    }

    @GetMapping("/bulk-ingredients/{ids}")
    @SneakyThrows
    public Collection<IngredientInfo> getIngredientsInBulk(
            @PathVariable("ids") Collection<Long> ids
    ) {
        List<IngredientInfo> infos = new ArrayList<>(ids.size());
        for (Long id : ids) {
            IngredientInfo info = getIngredientById(id);
            if (info != null) {
                infos.add(info);
            }
        }
        return infos;
    }

    // begin kludge (3 of 3)
    @Autowired private EntityManager em;
    @GetMapping("/or-ingredient/{id}")
    public IngredientInfo getIngredientById(@PathVariable("id") Long id) {
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
            info = ingredientMapper.recipeToInfo((Recipe) i);
        } else if (i instanceof PantryItem) {
            info = ingredientMapper.pantryItemToInfo((PantryItem) i);
        } else {
            info = ingredientMapper.ingredientToInfo(i);
        }

        return info;
    }
    // end kludge (3 of 3)

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecipe(@PathVariable Long id) {
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
    public void removeLabel(
        @PathVariable Long id,
        @PathVariable String label
    ) {
        Recipe recipe = getRecipe(id);
        labelService.removeLabel(recipe, label);
    }

    @PostMapping("/{id}/_send_to_plan/{planId}")
    @ResponseBody
    public Object performRecipeAction(
        @PathVariable("id") Long id,
        @PathVariable("planId") Long planId,
        @RequestParam(name = "scale", defaultValue = "1") Double scale
    ) {
        recipeService.sendToPlan(id, planId, scale);
        return true;
    }

    @GetMapping("/_index_stats")
    @PreAuthorize("hasRole('ROLE_DEVELOPER')")
    public IndexStats getIndexStats() {
        return recipeReindexQueueService.getIndexStats();
    }

    private Recipe getRecipe(Long id) {
        return recipeService.findRecipeById(id)
                .orElseThrow(NoResultException::new);
    }

    private IngredientInfo mapToInfo(String recipeData) throws IOException {
        return objectMapper.readValue(recipeData, IngredientInfo.class);
    }

}
