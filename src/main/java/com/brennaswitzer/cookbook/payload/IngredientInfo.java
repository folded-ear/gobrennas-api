package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;

import java.util.List;
import java.util.stream.Collectors;

public class IngredientInfo {

    public static class Ref {

        private String raw;
        private Float quantity;
        private String units;
        private Long ingredientId;
        private String preparation;

        public String getRaw() {
            return raw;
        }

        public void setRaw(String raw) {
            this.raw = raw;
        }

        public Float getQuantity() {
            return quantity;
        }

        public void setQuantity(Float quantity) {
            this.quantity = quantity;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
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

        public static Ref from(IngredientRef ref) {
            Ref info = new Ref();
            info.setRaw(ref.getRaw());
            info.setQuantity(ref.getQuantity());
            info.setUnits(ref.getUnits());
            info.setIngredientId(ref.hasIngredient()
                    ? ref.getIngredient().getId()
                    : null);
            info.setPreparation(ref.getPreparation());
            return info;
        }
    }

    private Long id;
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

    public static IngredientInfo from(Recipe r) {
        IngredientInfo info = from((AggregateIngredient) r);
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
        info.setId(it.getId());
        info.setName(it.getName());
        return info;
    }

}
