package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientInfo {

    @Deprecated
    public static class Ref extends IngredientRefInfo {
    }

    @Getter
    @Setter
    private Long id;
    @Getter
    @Setter
    private String type;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private Integer storeOrder;
    @Getter
    @Setter
    private String externalUrl;
    @Getter
    @Setter
    private String directions;
    @Getter
    @Setter
    private List<IngredientRefInfo> ingredients;
    @Setter
    private List<String> labels;
    @Getter
    @Setter
    private Long ownerId;
    @Getter
    @Setter
    private Integer yield;
    @Getter
    @Setter
    private Integer calories;
    @Getter
    @Setter
    private Integer totalTime;
    @Getter @Setter
    private String photo;
    @Getter @Setter
    private float[] photoFocus;
    @Getter @Setter
    private Boolean cookThis;

    public List<String> getLabels() {
        if (labels == null) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        return labels;
    }

    public boolean isCookThis() {
        return cookThis != null && cookThis;
    }

    public Recipe asRecipe(EntityManager em) {
        Recipe r = getId() == null
                ? new Recipe()
                : em.find(Recipe.class, getId());
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
        if (photoFocus != null && photoFocus.length == 2) {
            r.getPhoto(true).setFocusArray(photoFocus);
        }
        return r;
    }

}
