package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientInfo {

    @Deprecated
    public static class Ref extends IngredientRefInfo {
    }

    private Long id;
    private String type;
    private String name;
    private Integer storeOrder;
    private String externalUrl;
    private String directions;
    private List<IngredientRefInfo> ingredients;
    private List<String> labels;
    private Long ownerId;
    private Integer yield;
    private Integer calories;
    private Integer totalTime;
    private String photo;
    private float[] photoFocus;
    private Boolean cookThis;

    public List<String> getLabels() {
        return labels == null
                ? Collections.emptyList()
                : labels;
    }

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
        if (getIngredients() != null) {
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

}
