package com.brennaswitzer.cookbook.domain;

import javax.persistence.*;
import java.util.Comparator;

@Embeddable
public class IngredientRef<I extends Ingredient> implements Item {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) -> {
        String an = a.hasIngredient() ? a.getIngredient().getName() : a.getRaw();
        String bn = b.hasIngredient() ? b.getIngredient().getName() : b.getRaw();
        return an.compareToIgnoreCase(bn);
    };

    @Column(name = "_order")
    private int _idx;

    private String raw;

    @Embedded
    private Quantity quantity;

    private String preparation;

    @ManyToOne(targetEntity = Ingredient.class, cascade = {CascadeType.MERGE})
    private I ingredient;

    public IngredientRef() {}

    public IngredientRef(I ingredient) {
        this(null, ingredient, null);
    }

    public IngredientRef(Quantity quantity, I ingredient, String preparation) {
        setQuantity(quantity);
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

    public Quantity getQuantity() {
        if (quantity == null) return Quantity.ONE;
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public boolean hasQuantity() {
        return quantity != null;
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

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includePrep) {
        if (! hasIngredient()) return raw;
        StringBuilder sb = new StringBuilder();
        if (hasQuantity()) {
            sb.append(quantity).append(' ');
        }
        sb.append(ingredient.getName());
        if (includePrep && hasPreparation()) {
            sb.append(", ").append(preparation);
        }
        return sb.toString();
    }

}
