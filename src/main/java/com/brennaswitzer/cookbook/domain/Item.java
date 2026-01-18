package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.ValueUtils;

/**
 * Describes a single, atomic unit for a recipe, which could be
 * something like "3 oz Parmesan, shredded" or "2 each Pizza Dough, thawed".
 * The important thing is that the Item has structured information
 * about its quantity, unit, ingredient reference and also "other things
 * a cook might need to know" - quantified reference to an ingredient
 */
public interface Item {

    /**
     * Original raw string entered by input
     * @return raw string
     */
    String getRaw();

    /**
     * Quantity and unit pair that describes the "how much"
     * @return quantity/unit pair
     */
    Quantity getQuantity();

    default boolean hasQuantity() {
        return getQuantity() != null;
    }

    /**
     * The rest of the info needed to prep a plan item
     * @return instructions on prep
     */
    String getPreparation();

    default boolean hasPreparation() {
        return ValueUtils.hasValue(getPreparation());
    }

    /**
     * Reference to an ingredient, in this case most likely
     * a pantry item.
     * @return an ingredient
     */
    Ingredient getIngredient();

    default boolean hasIngredient() {
        return getIngredient() != null;
    }

    default String toRaw(boolean includePrep) {
        StringBuilder sb = new StringBuilder();
        if (hasQuantity()) {
            sb.append(getQuantity()).append(' ');
        }
        if (hasIngredient()) {
            sb.append(getIngredient().getName());
        }
        if (includePrep && hasPreparation()) {
            if (hasIngredient()) {
                sb.append(", ");
            }
            sb.append(getPreparation());
        }
        if (sb.isEmpty()) return getRaw();
        return sb.toString();
    }
}
