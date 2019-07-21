package com.brennaswitzer.cookbook.domain;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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

    @Column(name = "_order")
    private int _idx;

    private String raw;

    private Double quantity;
    private String units;
    private String preparation;

    @ManyToOne(targetEntity = Ingredient.class, cascade = {CascadeType.MERGE})
    private I ingredient;

    public IngredientRef() {}

    public IngredientRef(I ingredient) {
        this(null, null, ingredient, null);
    }

    public IngredientRef(int quantity, String units, I ingredient, String preparation) {
        this((double) quantity, units, ingredient, preparation);
    }

    public IngredientRef(Double quantity, String units, I ingredient, String preparation) {
        setQuantity(quantity);
        setUnits(units);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    int get_idx() {
        return _idx;
    }

    void set_idx(int _idx) {
        this._idx = _idx;
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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public boolean hasUnits() {
        return units != null && !units.isEmpty();
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public boolean hasPreparation() {
        return preparation != null && !preparation.isEmpty();
    }

    public Double getQuantity() {
        return quantity == null ? 1 : quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public boolean hasQuantity() {
        return quantity != null;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includePrep) {
        StringBuilder sb = new StringBuilder();
        if (hasQuantity()) {
            sb.append(quantity).append(' ');
        }
        if (hasUnits()) {
            sb.append(units).append(' ');
        }
        sb.append(ingredient.getName());
        if (includePrep && hasPreparation()) {
            sb.append(", ").append(preparation);
        }
        return sb.toString();
    }

}
