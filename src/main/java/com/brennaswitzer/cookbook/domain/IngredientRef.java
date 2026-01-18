package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Getter
@Setter
@Embeddable
@SuppressWarnings("JpaDataSourceORMInspection") // it's @Embeddable, and IntelliJ's too dumb
public class IngredientRef implements MutableItem {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) -> {
        String an = a.hasIngredient() ? a.getIngredient().getName() : a.getRaw();
        String bn = b.hasIngredient() ? b.getIngredient().getName() : b.getRaw();
        return an.compareToIgnoreCase(bn);
    };

    @Column(name = "_order")
    private int _idx;

    /**
     * Whether this ref represents a section (owned or by reference).
     */
    @Column(name = "is_section")
    private boolean section;

    private String raw;

    @Embedded
    private Quantity quantity;

    private String preparation;

    @ManyToOne( // if LAZY, the proxy confuses graphql-java
            targetEntity = Ingredient.class,
            cascade = { CascadeType.MERGE })
    private Ingredient ingredient;

    public IngredientRef() {
    }

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

    public String getRaw() {
        return raw == null ? toString() : raw;
    }

    public IngredientRef scale(Double scale) {
        if (scale <= 0) throw new IllegalArgumentException("Scaling by " + scale + " makes no sense?!");
        if (scale == 1 || !hasQuantity()) return this;
        return new IngredientRef(
                getQuantity().times(scale),
                getIngredient(),
                getPreparation());
    }

    @Override
    public String toString() {
        return toRaw(true);
    }

}
