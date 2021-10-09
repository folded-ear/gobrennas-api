package com.brennaswitzer.cookbook.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class InventoryItemInfo {

    Long id;
    IngredientInfo pantryItem;
    Collection<QuantityInfo> quantity;
    int txCount;

}
