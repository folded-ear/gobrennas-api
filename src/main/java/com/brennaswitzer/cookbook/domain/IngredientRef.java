package com.brennaswitzer.cookbook.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.util.Comparator;

@Embeddable
public class IngredientRef {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) ->
            Ingredient.BY_NAME.compare(a.getIngredient(), b.getIngredient());

    private String quantity;
    private String preparation;

    @ManyToOne(cascade = {CascadeType.MERGE})
    private Ingredient ingredient;

    public IngredientRef() {}

    public IngredientRef(Ingredient ingredient) {
        this(null, ingredient, null);
    }

    public IngredientRef(String quantity, Ingredient ingredient, String preparation) {
        setQuantity(quantity);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPreparation() {
        return preparation;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (quantity != null && quantity.length() > 0) {
            sb.append(quantity).append(' ');
        }
        sb.append(ingredient.getName());
        if (preparation != null && preparation.length() > 0) {
            sb.append(", ").append(preparation);
        }
        return sb.toString();
    }
}
