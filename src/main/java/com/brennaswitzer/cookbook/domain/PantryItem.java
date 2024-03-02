package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;

@Setter
@Getter
@Entity
@DiscriminatorValue("PantryItem")
@JsonTypeName("PantryItem")
public class PantryItem extends Ingredient {

    public static final Comparator<PantryItem> BY_STORE_ORDER = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        int c = a.getStoreOrder() - b.getStoreOrder();
        if (c != 0) {
            return c;
        }
        return a.getName().compareTo(b.getName());
    };

    private String aisle;

    // todo: make this user specific
    private int storeOrder = 999_999_999;

    public PantryItem() {}

    public PantryItem(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
