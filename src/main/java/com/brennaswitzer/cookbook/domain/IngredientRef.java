package com.brennaswitzer.cookbook.domain;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.util.Comparator;

@Embeddable
public class IngredientRef<I extends Ingredient> {

    public static Comparator<IngredientRef> BY_INGREDIENT_NAME = (a, b) ->
            Ingredient.BY_NAME.compare(a.getIngredient(), b.getIngredient());

    private String raw;
    private String quantity;
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

    public I getIngredient() {
        return ingredient;
    }

    public void setIngredient(I ingredient) {
        this.ingredient = ingredient;
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
        if (quantity != null && ! quantity.isEmpty()) {
            sb.append(quantity).append(' ');
        }
        sb.append(ingredient.getName());
        if (includePrep && preparation != null && ! preparation.isEmpty()) {
            sb.append(", ").append(preparation);
        }
        return sb.toString();
    }

    private String add(String a, String b, String def) {
        if (a == null || a.isEmpty()) a = def;
        if (b == null || b.isEmpty()) b = def;
        if (a == null) return b;
        if (b == null) return a;
        return a + ", " + b;
    }

    public IngredientRef<I> plus(IngredientRef<I> ref) {
        if (! ingredient.equals(ref.getIngredient())) {
            throw new IllegalArgumentException("You can't add IngredientRefs w/ different Ingredients");
        }
        return new IngredientRef<I>(
                add(quantity, ref.quantity, "1"),
                ingredient,
                add(preparation, ref.preparation, null)
        );
    }

}
