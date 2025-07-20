package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Photo;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.SearchResponse;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import com.brennaswitzer.cookbook.services.storage.ScratchSpace;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional
public class RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PlanService planService;

    @Autowired
    private UserPrincipalAccess principalAccess;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PlannedRecipeHistoryRepository historyRepository;

    @Autowired
    private ScratchSpace scratchSpace;

    public Recipe createNewRecipe(Recipe recipe) {
        return createNewRecipe(recipe, null);
    }

    public Recipe createNewRecipe(Recipe recipe, Upload photo) {
        return createNewRecipe(recipe, null, photo);
    }

    public Recipe createNewRecipe(Recipe recipe, IngredientInfo info, Upload photo) {
        recipe.setOwner(principalAccess.getUser());
        recipe = recipeRepository.save(recipe);
        new SetPhoto(info, photo)
                .set(recipe);
        return recipe;
    }

    public Recipe createNewRecipeFrom(Long sourceRecipeId, Recipe recipe, IngredientInfo info, Upload photo) {
        var source = recipeRepository.getReferenceById(sourceRecipeId);
        recipe.setOwner(principalAccess.getUser());
        recipe = recipeRepository.save(recipe);
        new SetPhoto(info, photo)
                .setOrCopy(source, recipe);
        return recipe;
    }

    public Recipe updateRecipe(Recipe recipe) {
        return this.updateRecipe(recipe, null);
    }

    public Recipe updateRecipe(Recipe recipe, Upload photo) {
        return updateRecipe(recipe, null, photo);
    }

    public Recipe updateRecipe(Recipe recipe, IngredientInfo info, Upload photo) {
        getMyRecipe(recipe.getId());
        new SetPhoto(info, photo)
                .set(recipe);
        return recipeRepository.save(recipe);
    }

    public Recipe setRecipePhoto(Long id, Upload photo) {
        return setRecipePhoto(id, null, null, photo);
    }

    public Recipe setRecipePhoto(Long id, String photoFilename, float[] photoFocus, Upload photo) {
        Recipe recipe = getMyRecipe(id);
        new SetPhoto(photoFilename, photoFocus, photo)
                .set(recipe);
        return recipeRepository.save(recipe);
    }

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
        recipe.getOwnedSections()
                .forEach(this::removeOwnedSection);
        recipeRepository.delete(recipe);
        return recipe;
    }

    /**
     * I change the passed section's owning recipe. If the section is not
     * referenced by any other recipes, it is deleted.
     */
    public void removeOwnedSection(Recipe section) {
        Recipe sectionOf = section.getSectionOf();
        if (sectionOf == null) {
            throw new IllegalStateException(String.format(
                    "Cannot remove non-owned section '%s' (%s)",
                    section.getId(),
                    section.getName()));
        }
        recipeRepository.searchRecipes(
                        LibrarySearchRequest.builder()
                                .user(sectionOf.getOwner())
                                .ingredientIds(Set.of(section.getId()))
                                .build())
                .getContent()
                .stream()
                .filter(r -> !sectionOf.equals(r))
                .findFirst()
                .ifPresentOrElse(
                        r -> r.addOwnedSection(section),
                        () -> {
                            sectionOf.removeOwnedSection(section);
                            recipeRepository.delete(section);
                        });
    }

    private final class SetPhoto {

        private final String filename;
        private final float[] focus;
        private final Upload upload;

        public SetPhoto(String filename, float[] focus, Upload upload) {
            if (upload != null) {
                log.warn("Deprecated photo upload (prefer a scratch file)");
                if (filename != null)
                    throw new IllegalArgumentException("Cannot specify a scratch filename AND upload a photo");
            }
            this.filename = filename;
            this.focus = focus;
            this.upload = upload;
        }

        public SetPhoto(IngredientInfo info,
                        Upload upload) {
            this(info == null ? null : info.getPhoto(),
                 info == null ? null : info.getPhotoFocus(),
                 upload);
        }

        public boolean set(Recipe recipe) {
            S3File s3File;
            if (filename != null) {
                var file = scratchSpace.verifyUpload(recipe.getOwner(), filename);
                var objectKey = buildObjectKey(recipe, file.filename());
                file.moveTo(objectKey);
                s3File = new S3File(
                        objectKey,
                        file.contentType(),
                        file.size());
            } else if (upload != null) {
                var objectKey = buildObjectKey(recipe, upload.getOriginalFilename());
                s3File = new S3File(
                        storageService.store(upload, objectKey),
                        upload.getContentType(),
                        upload.getSize());
            } else {
                return false;
            }
            removePhotoInternal(recipe);
            recipe.setPhoto(s3File);
            if (focus != null && focus.length == 2) {
                recipe.getPhoto().setFocusArray(focus);
            }
            return true;
        }

        public boolean setOrCopy(Recipe source, Recipe recipe) {
            if (set(recipe)) return true;
            if (source.hasPhoto()) {
                Photo photo = source.getPhoto();
                recipe.setPhoto(new S3File(
                        storageService.copy(photo.getObjectKey(),
                                            buildObjectKey(recipe,
                                                           photo.getFilename())),
                        photo.getContentType(),
                        photo.getSize()
                ));
                if (photo.hasFocus()) {
                    recipe.getPhoto().setFocusArray(photo.getFocusArray());
                }
                return true;
            }
            return false;
        }

        private String buildObjectKey(Recipe recipe, String name) {
            var id = recipe.getId();
            if (id == null) throw new IllegalArgumentException(
                    "Cannot build an object key for an unsaved recipe");
            return "recipe/" + id + "/" + S3File.sanitizeFilename(name);
        }

    }

    private void removePhotoInternal(Recipe recipe) {
        if (!recipe.hasPhoto()) return;
        storageService.remove(recipe.getPhoto().getObjectKey());
        recipe.clearPhoto();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public PlanItem sendToPlan(Long recipeId, Long planId, Double scale) {
        return planService.addRecipe(
                planId,
                recipeRepository.findById(recipeId).get(),
                scale);
    }

    /**
     * @deprecated prefer {@link #searchLibrary}
     */
    @Deprecated
    public SearchResponse<Recipe> searchRecipes(LibrarySearchScope scope,
                                                String filter,
                                                Set<Long> ingredientIds,
                                                int offset,
                                                int limit) {
        return searchInternal(
                LibrarySearchRequest.builder()
                        .scope(scope)
                        .filter(filter)
                        .ingredientIds(ingredientIds)
                        .offset(offset)
                        .limit(limit));
    }

    public SearchResponse<Recipe> searchLibrary(
            LibrarySearchRequest req) {
        return searchInternal(req.toBuilder());
    }

    private SearchResponse<Recipe> searchInternal(
            LibrarySearchRequest.LibrarySearchRequestBuilder reqBuilder) {
        return recipeRepository.searchRecipes(
                reqBuilder
                        .user(principalAccess.getUser())
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
