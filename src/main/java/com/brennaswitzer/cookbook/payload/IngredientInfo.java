package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientInfo extends CoreRecipeInfo {

    private String type;
    private Integer storeOrder;
    private String externalUrl;
    private List<SectionInfo> sections;
    private Long ownerId;
    private Integer yield;
    private Integer calories;
    private Integer totalTime;
    private String photo;
    private float[] photoFocus;
    private Boolean cookThis;

    public boolean isCookThis() {
        return cookThis != null && cookThis;
    }

    public Recipe asRecipe(EntityManager em) {
        Recipe r = getId() == null
                ? new Recipe()
                : em.getReference(Recipe.class, getId());
        r.setName(getName());
        r.setExternalUrl(getExternalUrl());
        r.setDirections(getDirections());
        r.setYield(getYield());
        r.setTotalTime(getTotalTime());
        r.setCalories(getCalories());
        if (hasIngredients()) {
            r.setIngredients(getIngredients()
                    .stream()
                    .map(ref -> ref.asIngredientRef(em))
                    .collect(Collectors.toList()));
        }
        // photo is NOT copied, as the S3 object needs to move too
        if (photoFocus != null && photoFocus.length == 2) {
            r.getPhoto(true).setFocusArray(photoFocus);
        }
        return r;
    }

    public boolean hasSections() {
        return getSections() != null && !getSections().isEmpty();
    }

}
