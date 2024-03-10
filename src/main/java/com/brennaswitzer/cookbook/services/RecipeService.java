package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.Upload;
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

import java.util.Optional;

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

    public Recipe createNewRecipe(Recipe recipe) {
        return createNewRecipe(recipe, null);
    }

    public Recipe createNewRecipe(Recipe recipe, Upload photo) {
        recipe.setOwner(principalAccess.getUser());
        recipe = recipeRepository.save(recipe);
        if (photo != null) setPhotoInternal(recipe, photo);
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

    public void deleteRecipeById(Long id) {
        Recipe recipe = getMyRecipe(id);
        removePhotoInternal(recipe);
        planService.severLibraryLinks(recipe);
        recipeRepository.delete(recipe);
    }

    private void setPhotoInternal(Recipe recipe, Upload photo) {
        removePhotoInternal(recipe);
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

    private void removePhotoInternal(Recipe recipe) {
        if (!recipe.hasPhoto()) return;
        storageService.remove(recipe.getPhoto().getObjectKey());
        recipe.clearPhoto();
    }

    public void sendToPlan(Long recipeId, Long planId) {
        sendToPlan(recipeId, planId, 1d);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void sendToPlan(Long recipeId, Long planId, Double scale) {
        planService.addRecipe(planId,
                              recipeRepository.findById(recipeId).get(),
                              scale);
    }

    public SearchResponse<Recipe> searchRecipes(String scope,
                                                String filter,
                                                int offset,
                                                int limit) {
        return searchRecipes(LibrarySearchScope.valueOf(scope.toUpperCase()),
                             filter,
                             offset,
                             limit);
    }

    public SearchResponse<Recipe> searchRecipes(LibrarySearchScope scope,
                                                String filter,
                                                int offset,
                                                int limit) {
        return recipeRepository.searchRecipes(
                LibrarySearchRequest.builder()
                        .user(principalAccess.getUser())
                        .scope(scope)
                        .filter(filter)
                        .offset(offset)
                        .limit(limit)
                        .build());
    }

}
