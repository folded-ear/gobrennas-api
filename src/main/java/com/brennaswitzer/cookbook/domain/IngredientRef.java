package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.NumberUtils;

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

    private String quantity;
    private String units;
    private String preparation;

    private Float amount;

    @ManyToOne(targetEntity = Ingredient.class, cascade = {CascadeType.MERGE})
    private I ingredient;

    public IngredientRef() {}

    public IngredientRef(I ingredient) {
        this(null, null, ingredient, null);
    }

    public IngredientRef(String quantity, String units, I ingredient, String preparation) {
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

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
        this.amount = NumberUtils.parseFloat(quantity);
    }

    public boolean hasQuantity() {
        return quantity != null && !quantity.isEmpty();
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

    public Float getAmount() {
        return amount;
    }

    public void setAmount(Float amount) {
        this.amount = amount;
    }

    public boolean hasAmount() {
        return amount != null;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean includePrep) {
        StringBuilder sb = new StringBuilder();
        if (hasAmount()) {
            sb.append(amount).append(' ');
        } else if (hasQuantity()) {
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
