package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PantryItem")
@JsonTypeName("PantryItem")
public class PantryItem extends Ingredient {

    private String aisle;

    // todo: make this user specific
    private int storeOrder;

    public PantryItem() {}

    public PantryItem(String name) {
        super(name);
    }

    public String getAisle() {
        return aisle;
    }

    public void setAisle(String aisle) {
        this.aisle = aisle;
    }

    public int getStoreOrder() {
        return storeOrder;
    }

    public void setStoreOrder(int storeOrder) {
        this.storeOrder = storeOrder;
    }

    @Override
    public String toString() {
        return getName();
    }
}