package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.Label;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface LabelMapper {

    @Mapping(target = ".", source = "it.name")
    String labelToString(Label it);

//    List<String> labelsToStrings(Set<Label> value);

}
