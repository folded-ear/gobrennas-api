package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.IngredientRefInfo;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = {
                LabelMapper.class,
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE // PantryItem is pretty anemic vs IngredientInfo
)
@DecoratedWith(IngredientMapperDecorator.class)
public interface IngredientMapper {

    @Mapping(target = "quantity", source = "ref.quantity.quantity")
    @Mapping(target = "units", source = "ref.quantity.units.name")
    @Mapping(target = "uomId", source = "ref.quantity.units.id")
    @Mapping(target = "ingredient", source = "ref.ingredient.name")
    @Mapping(target = "ingredientId", source = "ref.ingredient.id")
    IngredientRefInfo refToInfo(IngredientRef ref);

    @Mapping(target = "type", constant = "PantryItem")
    IngredientInfo pantryItemToInfo(PantryItem it);

    @Mapping(target = "type", constant = "Recipe")
    @Mapping(target = "photo", ignore = true)
    IngredientInfo recipeToInfo(Recipe r);

    IngredientInfo ingredientToInfo(Ingredient it);


}
