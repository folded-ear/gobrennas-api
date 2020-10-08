package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Comparator;

@Entity
@DiscriminatorValue("PantryItem")
@JsonTypeName("PantryItem")
public class PantryItem extends Ingredient {

    public static final Comparator<PantryItem> BY_STORE_ORDER = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        if (a.storeOrder != b.storeOrder) return a.storeOrder - b.storeOrder;
        return a.getName().compareTo(b.getName());
    };

    private String aisle;

    // todo: make this user specific
    private int storeOrder = 999_999_999;

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