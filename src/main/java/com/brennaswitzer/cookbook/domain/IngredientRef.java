package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;

@Embeddable
@SuppressWarnings("JpaDataSourceORMInspection") // it's @Embeddable, and IntelliJ's too dumb
public class IngredientRef implements MutableItem {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) -> {
        String an = a.hasIngredient() ? a.getIngredient().getName() : a.getRaw();
        String bn = b.hasIngredient() ? b.getIngredient().getName() : b.getRaw();
        return an.compareToIgnoreCase(bn);
    };

    @Column(name = "_order")
    @Getter
    @Setter
    private int _idx;

    @Setter
    private String raw;

    @Embedded
    @Setter
    private Quantity quantity;

    @Getter
    @Setter
    private String preparation;

    @ManyToOne(targetEntity = Ingredient.class, cascade = {CascadeType.MERGE})
    @Getter
    @Setter
    private Ingredient ingredient;

    public IngredientRef() {}

    public IngredientRef(Ingredient ingredient) {
        this(null, ingredient, null);
    }

    public IngredientRef(Quantity quantity, Ingredient ingredient, String preparation) {
        setQuantity(quantity);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    public IngredientRef(String raw) {
        setRaw(raw);
    }

    public boolean hasIngredient() {
        return ingredient != null;
    }

    public String getRaw() {
        return raw == null ? toString() : raw;
    }

    public Quantity getQuantity() {
        if (quantity == null) return Quantity.ONE;
        return quantity;
    }

    public boolean hasQuantity() {
        return quantity != null;
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
