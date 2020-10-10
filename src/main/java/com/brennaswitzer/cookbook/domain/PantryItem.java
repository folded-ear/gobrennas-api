package com.brennaswitzer.cookbook.domain;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

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

    @Getter
    @Setter
    private String aisle;

    // todo: make this user specific
    @Getter
    @Setter
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