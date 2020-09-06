package com.brennaswitzer.cookbook.domain;

/**
 * Describes a single, atomic unit for a recipe, which could be
 * something like "3 oz parmesan, shredded" or "2 each Pizza Dough, thawed".
 * The important thing is that the Item has structured information
 * about it's amount, unit, ingredient reference and also "other things
 * a cook might need to know" - quanitified reference to an ingredient
 */
public interface Item {

    /**
     * Original raw string entered by input
     * @return raw string
     */
    String getRaw();

    /**
     * Amount and unit pair that describes the "how much"
     * @return amount/unit pair
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
