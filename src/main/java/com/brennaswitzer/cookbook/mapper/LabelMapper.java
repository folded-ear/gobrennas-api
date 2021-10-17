package com.brennaswitzer.cookbook.mapper;

import com.brennaswitzer.cookbook.domain.Label;
import org.mapstruct.Mapper;

@Mapper
public interface LabelMapper {

    default String labelToString(Label it) {
        return it.getName();
    }

//    List<String> labelsToStrings(Set<Label> value);

}
