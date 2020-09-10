package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.*;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientInfo {

    public static class Ref {

        private String raw;
        private Double quantity;
        private String units;
        private Long uomId;
        private String ingredient;
        private Long ingredientId;
        private String preparation;

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }

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

        public Long getUomId() {
            return uomId;
        }

        public void setUomId(Long uomId) {
            this.uomId = uomId;
        }

        public boolean hasUomId() {
            return uomId != null;
        }

        public String getIngredient() {
            return ingredient;
        }

        public void setIngredient(String ingredient) {
            this.ingredient = ingredient;
        }

        public boolean hasIngredient() {
            return this.ingredient != null;
        }

        public Long getIngredientId() {
            return ingredientId;
        }

        public void setIngredientId(Long ingredientId) {
            this.ingredientId = ingredientId;
        }

        public boolean hasIngredientId() {
            return this.ingredientId != null;
        }

        public String getPreparation() {
            return preparation;
        }

        public void setPreparation(String preparation) {
            this.preparation = preparation;
        }

        public IngredientRef asIngredientRef(EntityManager em) {
            IngredientRef<Ingredient> ref = new IngredientRef<>();
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

    private Long id;
    private String type;
    private String name;
    private String externalUrl;
    private String directions;
    private List<Ref> ingredients;
    private List<String> labels;
    private Long ownerId;
    private Integer yield;
    private Integer calories;
    private Integer totalTime;
    private boolean cookThis;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getDirections() {
        return directions;
    }

    public void setDirections(String directions) {
        this.directions = directions;
    }

    public List<Ref> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ref> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getLabels() {
        if (labels == null) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getYield() {
        return yield;
    }

    public void setYield(Integer yield) {
        this.yield = yield;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public boolean isCookThis() {
        return cookThis;
    }

    public void setCookThis(boolean cookThis) {
        this.cookThis = cookThis;
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
        info.setLabels(r.getLabels()
                .stream()
                .map(Label::getName)
                .collect(Collectors.toList()));
        if (r.getOwner() != null) {
            info.setOwnerId(r.getOwner().getId());
        }
        return info;
    }

    public static IngredientInfo from(AggregateIngredient it) {
        IngredientInfo info = new IngredientInfo();
        info.setId(it.getId());
        info.setName(it.getName());
        if (it.getIngredients() != null) {
            info.setIngredients(it.getIngredients()
                    .stream()
                    .map(Ref::from)
                    .collect(Collectors.toList()));
        }
        return info;
    }

    public static IngredientInfo from(PantryItem it) {
        IngredientInfo info = new IngredientInfo();
        info.setType("PantryItem");
        info.setId(it.getId());
        info.setName(it.getName());
        return info;
    }

    public static List<Recipe> fromRecipes(Iterable<Recipe> iterable) {
        List<Recipe> recipes = new LinkedList<>();
        iterable.forEach(recipes::add);
        return recipes;
    }
}
