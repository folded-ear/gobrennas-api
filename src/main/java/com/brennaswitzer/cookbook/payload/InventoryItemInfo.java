package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.mapper.QuantityMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.util.Collection;

@Getter
@Setter
public class InventoryItemInfo {

    Long id;
    IngredientInfo pantryItem;
    Collection<QuantityInfo> quantity;
    int txCount;

    public static InventoryItemInfo fromItem(InventoryItem it) {
        val info = new InventoryItemInfo();
        info.setId(it.getId());
        info.setPantryItem(IngredientInfo.from(it.getPantryItem()));
        info.setQuantity(QuantityMapper.INSTANCE.compoundQuantityToInfos(it.getQuantity()));
        info.setTxCount(it.getTxCount());
        return info;
    }

}
