package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientInfo {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Ref {

        @Getter @Setter
        private String raw;
        @Getter @Setter
        private Double quantity;
        private String units;
        @Getter @Setter
        private Long uomId;
        @Getter @Setter
        private String ingredient;
        @Getter @Setter
        private Long ingredientId;
        @Getter @Setter
        private String preparation;

        public boolean hasQuantity() {
            return quantity != null;
        }

        @Deprecated
        public String getUnits() {
            return units;
        }

        @Deprecated
        public void setUnits(String units) {
            this.units = units;
        }

        @Deprecated
        public boolean hasUnits() {
            return units != null && !"".equals(units) && !units.trim().isEmpty();
        }

        public boolean hasUomId() {
            return uomId != null;
        }

        public boolean hasIngredient() {
            return this.ingredient != null;
        }

        public boolean hasIngredientId() {
            return this.ingredientId != null;
        }

        public IngredientRef asIngredientRef(EntityManager em) {
            IngredientRef ref = new IngredientRef();
            ref.setRaw(getRaw());
            if (hasQuantity()) {
                UnitOfMeasure uom = hasUomId()
                        ? em.find(UnitOfMeasure.class, getUomId())
                        : hasUnits()
                        ? UnitOfMeasure.ensure(em, getUnits())
                        : null;
                ref.setQuantity(new Quantity(getQuantity(), uom));
            }
            ref.setPreparation(getPreparation());
            if (hasIngredientId()) {
                ref.setIngredient(em.find(Ingredient.class, getIngredientId()));
            } else if (hasIngredient()) {
                PantryItem it = new PantryItem(getIngredient());
                em.persist(it);
                ref.setIngredient(it);
            }
            return ref;
        }

        public static Ref from(IngredientRef ref) {
            Ref info = new Ref();
            info.setRaw(ref.getRaw());
            if (ref.hasQuantity()) {
                Quantity q = ref.getQuantity();
                info.setQuantity(q.getQuantity());
                if (q.hasUnits()) {
                    info.setUomId(q.getUnits().getId());
                    info.setUnits(q.getUnits().getName());
                }
            }
            if (ref.hasIngredient()) {
                info.setIngredientId(ref.getIngredient().getId());
                info.setIngredient(ref.getIngredient().getName());
            }
            info.setPreparation(ref.getPreparation());
            return info;
        }

    }

    @Getter @Setter
    private Long id;
    @Getter @Setter
    private String type;
    @Getter @Setter
    private String name;
    @Getter @Setter
    private Integer storeOrder;
    @Getter @Setter
    private String externalUrl;
    @Getter @Setter
    private String directions;
    @Getter @Setter
    private List<Ref> ingredients;
    @Setter
    private List<String> labels;
    @Getter @Setter
    private Long ownerId;
    @Getter @Setter
    private Integer yield;
    @Getter @Setter
    private Integer calories;
    @Getter @Setter
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

    public static IngredientInfo from(Recipe r) {
        IngredientInfo info = from((AggregateIngredient) r);
        info.setType("Recipe");
        info.setExternalUrl(r.getExternalUrl());
        info.setDirections(r.getDirections());
        info.setYield(r.getYield());
        info.setTotalTime(r.getTotalTime());
        info.setCalories(r.getCalories());
        if (r.getOwner() != null) {
            info.setOwnerId(r.getOwner().getId());
        }
        return info;
    }

    public static IngredientInfo from(AggregateIngredient it) {
        IngredientInfo info = from((Ingredient) it);
        if (it.getIngredients() != null) {
            info.setIngredients(it.getIngredients()
                    .stream()
                    .map(Ref::from)
                    .collect(Collectors.toList()));
        }
        return info;
    }

    public static IngredientInfo from(Ingredient it) {
        IngredientInfo info = new IngredientInfo();
        info.setId(it.getId());
        info.setName(it.getName());
        if (it.hasLabels()) {
            info.setLabels(it.getLabels()
                    .stream()
                    .map(Label::getName)
                    .collect(Collectors.toList()));
        }
        return info;
    }

}
