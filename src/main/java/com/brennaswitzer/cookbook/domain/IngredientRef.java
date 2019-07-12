package com.brennaswitzer.cookbook.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.util.Comparator;

@Embeddable
public class IngredientRef<I extends Ingredient> {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) -> {
        String an = a.hasIngredient() ? a.getIngredient().getName() : a.getRaw();
        String bn = b.hasIngredient() ? b.getIngredient().getName() : b.getRaw();
        return an.compareToIgnoreCase(bn);
    };

    private String raw;
    private String quantity;
    private String units;
    private String preparation;

    @ManyToOne(targetEntity = Ingredient.class, cascade = {CascadeType.MERGE})
    private I ingredient;

    public IngredientRef() {}

    public IngredientRef(I ingredient) {
        this(null, ingredient, null);
    }

    public IngredientRef(String quantity, I ingredient, String preparation) {
        setQuantity(quantity);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    public IngredientRef(String raw) {
        setRaw(raw);
    }

    public I getIngredient() {
        return ingredient;
    }

    public void setIngredient(I ingredient) {
        this.ingredient = ingredient;
    }

    public boolean hasIngredient() {
        return ingredient != null;
    }

    public String getRaw() {
        return raw == null ? toString() : raw;
    }

    public void setRaw(String raw) {
        this.raw = raw;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includePrep) {
        StringBuilder sb = new StringBuilder();
        if (quantity != null && !quantity.isEmpty()) {
            sb.append(quantity).append(' ');
        }
        if (units != null && !units.isEmpty()) {
            sb.append(units).append(' ');
        }
        sb.append(ingredient.getName());
        if (includePrep && preparation != null && !preparation.isEmpty()) {
            sb.append(", ").append(preparation);
        }
        return sb.toString();
    }

}
