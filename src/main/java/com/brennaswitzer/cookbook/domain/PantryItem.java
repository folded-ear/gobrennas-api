package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PantryItem")
@JsonTypeName("PantryItem")
public class PantryItem extends Ingredient {

    private String aisle;

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

    @Override
    public String toString() {
        return getName();
    }
}