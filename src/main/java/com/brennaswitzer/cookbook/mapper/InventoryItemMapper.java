package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.domain.InventoryTx;
import com.brennaswitzer.cookbook.payload.InventoryItemInfo;
import com.brennaswitzer.cookbook.payload.InventoryTxInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        IngredientMapper.class,
        QuantityMapper.class,
        LabelMapper.class,
})
public interface InventoryItemMapper {

    InventoryItemMapper INSTANCE = Mappers.getMapper(InventoryItemMapper.class);

    InventoryItemInfo itemToInfo(InventoryItem item);

    @Mapping(target = "ingredient", source = "tx.item.ingredient.name")
    @Mapping(target = "ingredientId", source = "tx.item.ingredient.id")
    InventoryTxInfo txToInfo(InventoryTx tx);

}
