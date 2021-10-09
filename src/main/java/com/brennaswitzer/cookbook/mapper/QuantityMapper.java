package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.CompoundQuantity;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.payload.QuantityInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collection;
import java.util.List;

@Mapper
public interface QuantityMapper {

    QuantityMapper INSTANCE = Mappers.getMapper(QuantityMapper.class);

    @Mapping(target = "units", source = "q.units.name")
    @Mapping(target = "uomId", source = "q.units.id")
    QuantityInfo quantityToInfo(Quantity q);

    List<QuantityInfo> quantitiesToInfos(Collection<Quantity> qs);

    default List<QuantityInfo> compoundQuantityToInfos(CompoundQuantity q) {
        return INSTANCE.quantitiesToInfos(q.getComponents());
    }

}
