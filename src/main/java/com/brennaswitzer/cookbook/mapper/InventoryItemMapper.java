package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.InventoryItem;
import com.brennaswitzer.cookbook.payload.InventoryItemInfo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {
        PantryItemMapper.class,
        QuantityMapper.class,
})
public interface InventoryItemMapper {

    InventoryItemMapper INSTANCE = Mappers.getMapper(InventoryItemMapper.class);

    InventoryItemInfo itemToInfo(InventoryItem item);

}
