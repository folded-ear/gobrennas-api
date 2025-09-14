package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.payload.CoreRecipeInfo;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.IngredientRefInfo;
import com.brennaswitzer.cookbook.payload.SectionInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.ItemService;
import com.brennaswitzer.cookbook.services.LabelService;
import com.brennaswitzer.cookbook.services.RecipeService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class Info2Recipe {

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private EntityManager entityManager;

    public Recipe convert(UserPrincipal owner,
                          IngredientInfo info) {
        return convert(owner, info, false);
    }

    public Recipe convert(UserPrincipal owner,
                          IngredientInfo info,
                          boolean cookThis) {
        return new Convert(cookThis,
                           entityManager.find(User.class,
                                              owner.getId()))
                .toRecipe(info);
    }

    private class Convert {

        private final boolean cookThis;
        private final User owner;

        private Convert(boolean cookThis, User owner) {
            this.cookThis = cookThis;
            this.owner = owner;
        }

        public Recipe toRecipe(IngredientInfo info) {
            Recipe r;
            if (info.getId() == null) {
                r = newRecipe();
            } else {
                r = loadRecipe(info);
                if (!r.getOwner().equals(owner)) {
                    throw new AccessDeniedException("You can only modify your own recipes.");
                }
                // remove no-longer-present owned sections
                Collection<Recipe> toRemove;
                if (info.hasSections()) {
                    Set<Long> idsToRetain = info.getSections()
                            .stream()
                            .map(SectionInfo::getId)
                            .collect(Collectors.toSet());
                    toRemove = r.getOwnedSections()
                            .stream()
                            .filter(s -> s.getId() != null)
                            .filter(s -> !idsToRetain.contains(s.getId()))
                            .toList();
                } else {
                    // copy the collection to consume while iterating
                    toRemove = List.copyOf(r.getOwnedSections());
                }
                toRemove.forEach(recipeService::removeOwnedSection);
            }
            setCoreInfo(info, r);
            if (info.hasSections()) {
                for (var s : info.getSections()) {
                    toRecipeUnder(s, r);
                }
            }
            r.setExternalUrl(info.getExternalUrl());
            r.setYield(info.getYield());
            r.setTotalTime(info.getTotalTime());
            r.setCalories(info.getCalories());
            // photo is NOT copied, as the S3 object needs to move too
            List<Float> photoFocus = info.getPhotoFocus();
            if (photoFocus != null && photoFocus.size() == 2) {
                r.getPhoto(true).setFocus(photoFocus);
            }
            return r;
        }

        private void setCoreInfo(CoreRecipeInfo info, Recipe r) {
            r.setName(info.getName());
            r.setDirections(info.getDirections());
            if (info.hasIngredients()) {
                r.setIngredients(info.getIngredients()
                                         .stream()
                                         .map(this::toIngredientRef)
                                         .collect(Collectors.toList()));
                if (cookThis) {
                    r.getIngredients().forEach(itemService::autoRecognize);
                }
            }
            labelService.updateLabels(r, info.getLabels());
        }

        private IngredientRef toIngredientRef(IngredientRefInfo info) {
            IngredientRef ref = new IngredientRef();
            ref.setRaw(info.getRaw());
            if (info.hasQuantity()) {
                ref.setQuantity(info.extractQuantity(entityManager));
            }
            ref.setPreparation(info.getPreparation());
            if (info.hasIngredientId()) {
                // don't know what type it is...
                ref.setIngredient(entityManager.find(Ingredient.class,
                                                     info.getIngredientId()));
            } else if (info.hasIngredient()) {
                PantryItem it = new PantryItem(info.getIngredient());
                entityManager.persist(it);
                ref.setIngredient(it);
            }
            return ref;
        }

        private void toRecipeUnder(SectionInfo info,
                                   Recipe parent) {
            Recipe s;
            if (info.getId() == null) {
                s = newRecipe();
                parent.addOwnedSection(s);
            } else {
                // cross-user references are fine
                s = loadRecipe(info);
                parent.addIngredient(s)
                        .setSection(true);
                if (!s.isOwnedSection() || !s.getSectionOf().equals(parent)) {
                    // not owned, just a reference
                    return;
                }
            }
            setCoreInfo(info, s);
        }

        private Recipe newRecipe() {
            Recipe r = new Recipe();
            r.setOwner(owner);
            entityManager.persist(r);
            return r;
        }

        private Recipe loadRecipe(Identified info) {
            return entityManager.find(Recipe.class,
                                      info.getId());
        }

    }

}
