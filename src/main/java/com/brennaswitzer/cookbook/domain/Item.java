package com.brennaswitzer.cookbook.domain;

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

    /**
     * The rest of the info needed to prep a plan item
     * @return instructions on prep
     */
    String getPreparation();

    /**
     * Reference to an ingredient, in this case most likely
     * a pantry item.
     * @return an ingredient
     */
    Ingredient getIngredient();
}
