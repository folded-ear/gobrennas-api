package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccessLevelConverter extends AbstractIdentifiedEnumAttributeConverter<AccessLevel> {

    public AccessLevelConverter() {
        super(AccessLevel.class);
    }

}
