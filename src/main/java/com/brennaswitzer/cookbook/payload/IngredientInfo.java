package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.*;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

public class IngredientInfo {

    public static class Ref {

        private String raw;
        private Double quantity;
        private String units;
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

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public boolean hasUnits() {
            return units != null && !"".equals(units) && !units.trim().isEmpty();
        }

        public Long getIngredientId() {
            return ingredientId;
        }

        public void setIngredientId(Long ingredientId) {
            this.ingredientId = ingredientId;
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
                UnitOfMeasure uom = hasUnits()
                        ? UnitOfMeasure.ensure(em, getUnits())
                        : null;
                ref.setQuantity(new Quantity(getQuantity(), uom));
            }
            ref.setPreparation(getPreparation());
            if (getIngredientId() != null) {
                ref.setIngredient(em.find(Ingredient.class, getIngredientId()));
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
                    info.setUnits(q.getUnits().getName());
                }
            }
            info.setIngredientId(ref.hasIngredient()
                    ? ref.getIngredient().getId()
                    : null);
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

    public Recipe asRecipe(EntityManager em) {
        Recipe r = getId() == null
                ? new Recipe()
                : em.find(Recipe.class, getId());
        r.setName(getName());
        r.setExternalUrl(getExternalUrl());
        r.setDirections(getDirections());
        r.setIngredients(getIngredients()
                .stream()
                .map(ref -> ref.asIngredientRef(em))
                .collect(Collectors.toList()));
        return r;
    }

    public static IngredientInfo from(Recipe r) {
        IngredientInfo info = from((AggregateIngredient) r);
        info.setType("Recipe");
        info.setExternalUrl(r.getExternalUrl());
        info.setDirections(r.getDirections());
        return info;
    }

    public static IngredientInfo from(AggregateIngredient it) {
        IngredientInfo info = new IngredientInfo();
        info.setId(it.getId());
        info.setName(it.getName());
        info.setIngredients(it.getIngredients()
                .stream()
                .map(Ref::from)
                .collect(Collectors.toList()));
        return info;
    }

    public static IngredientInfo from(PantryItem it) {
        IngredientInfo info = new IngredientInfo();
        info.setType("PantryItem");
        info.setId(it.getId());
        info.setName(it.getName());
        return info;
    }

}
