package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {
                LabelMapper.class,
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE // PantryItem is pretty anemic vs IngredientInfo
)
public interface PantryItemMapper {

    @Mapping(target = "type", constant = "PantryItem")
    IngredientInfo pantryItemToInfo(PantryItem it);

}
