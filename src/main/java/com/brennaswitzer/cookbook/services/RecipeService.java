package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    IngredientService ingredientService;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PlannedRecipeHistoryRepository historyRepository;

    public Recipe createNewRecipe(Recipe recipe) {
        return createNewRecipe(recipe, null);
    }

    public Recipe createNewRecipe(Recipe recipe, Upload photo) {
        recipe.setOwner(principalAccess.getUser());
        recipe = recipeRepository.save(recipe);
        if (photo != null) setPhotoInternal(recipe, photo);
        return recipe;
    }

    public Recipe createNewRecipeFrom(Long sourceRecipeId, Recipe recipe, Upload photo) {
        var source = recipeRepository.getReferenceById(sourceRecipeId);
        recipe.setOwner(principalAccess.getUser());
        recipe = recipeRepository.save(recipe);
        if (photo != null) {
            setPhotoInternal(recipe, photo);
        } else if (source.hasPhoto()) {
            copyPhoto(source, recipe);
        }
        return recipe;
    }

    public Recipe updateRecipe(Recipe recipe) {
        return this.updateRecipe(recipe, null);
    }

    public Recipe updateRecipe(Recipe recipe, Upload photo) {
        getMyRecipe(recipe.getId());
        if (photo != null) setPhotoInternal(recipe, photo);
        return recipeRepository.save(recipe);
    }

    public Recipe setRecipePhoto(Long id, Upload photo) {
        Recipe recipe = getMyRecipe(id);
        setPhotoInternal(recipe, photo);
        return recipeRepository.save(recipe);
    }

    @NotNull
    private Recipe getMyRecipe(Long id) {
        Recipe recipe = recipeRepository.getReferenceById(id);
        if (!recipe.getOwner().equals(principalAccess.getUser())) {
            throw new AccessDeniedException("You can only modify your own recipes.");
        }
        return recipe;
    }

    public Optional<Recipe> findRecipeById(Long id) {
        return recipeRepository.findById(id);
    }

    public Recipe deleteRecipeById(Long id) {
        Recipe recipe = getMyRecipe(id);
        removePhotoInternal(recipe);
        planService.severLibraryLinks(recipe);
        recipeRepository.delete(recipe);
        return recipe;
    }

    private void setPhotoInternal(Recipe recipe, Upload photo) {
        removePhotoInternal(recipe);
        String name = photo.getOriginalFilename();
        if (name == null) {
            name = "photo";
        } else {
            name = S3File.sanitizeFilename(name);
        }
        recipe.setPhoto(new S3File(
                storageService.store(photo,
                                     buildObjectKey(recipe, name)),
                photo.getContentType(),
                photo.getSize()
        ));
    }

    private void copyPhoto(Recipe source, Recipe dest) {
        removePhotoInternal(dest);
        if (!source.hasPhoto()) return; // silly caller
        Photo photo = source.getPhoto();
        dest.setPhoto(new S3File(
                storageService.copy(photo.getObjectKey(),
                                    buildObjectKey(dest, photo.getFilename())),
                photo.getContentType(),
                photo.getSize()
        ));
        if (photo.hasFocus()) {
            dest.getPhoto().setFocusArray(photo.getFocusArray());
        }
    }

    private String buildObjectKey(Recipe recipe, String name) {
        return "recipe/" + recipe.getId() + "/" + name;
    }

    private void removePhotoInternal(Recipe recipe) {
        if (!recipe.hasPhoto()) return;
        storageService.remove(recipe.getPhoto().getObjectKey());
        recipe.clearPhoto();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId, Double scale) {
        planService.addRecipe(planId,
                              recipeRepository.findById(recipeId).get(),
                              scale);
    }


    public SearchResponse<Recipe> searchRecipes(LibrarySearchScope scope,
                                                String filter,
                                                Set<Long> ingredientIds,
                                                int offset,
                                                int limit) {
        return recipeRepository.searchRecipes(
                LibrarySearchRequest.builder()
                        .user(principalAccess.getUser())
                        .scope(scope)
                        .filter(filter)
                        .ingredientIds(ingredientIds)
                        .offset(offset)
                        .limit(limit)
                        .build());
    }

    public SearchResponse<Recipe> suggestRecipes(int offset, int limit) {
        LibrarySearchRequest req = LibrarySearchRequest.builder()
                .user(principalAccess.getUser())
                .offset(offset)
                .limit(limit)
                .build();
        List<Long> ids = historyRepository.getRecipeSuggestions(
                req.getUser().getId(),
                req.getOffset(),
                req.getLimit() + 1); // one extra (for SearchResponse)
        var byId = recipeRepository.findByIdIn(ids).stream()
                .collect(Collectors.toMap(
                        Recipe::getId,
                        Function.identity()));
        List<Recipe> recipes = ids.stream()
                .map(byId::get)
                .toList();
        return SearchResponse.of(req, recipes);
    }

}
