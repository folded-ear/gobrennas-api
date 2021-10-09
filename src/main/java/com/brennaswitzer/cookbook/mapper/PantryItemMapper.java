package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {
        LabelMapper.class,
})
public interface PantryItemMapper {

    @Mapping(target = "type", constant = "PantryItem")
    IngredientInfo pantryItemToInfo(PantryItem it);

}
