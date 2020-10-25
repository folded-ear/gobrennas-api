package com.brennaswitzer.cookbook.domain;

public interface MutableItem extends Item {

    // I have omitted setRaw(String) from the interface, as I believe changes to
    // the raw string are an implementation detail. But moving from the raw
    // string to the other properties is definitely Item-ish. But that's up
    // for discussion. :)

    void setQuantity(Quantity q);

    void setPreparation(String prep);

    void setIngredient(Ingredient ing);

}
