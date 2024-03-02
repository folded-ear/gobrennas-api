package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccessLevelConverter extends AbstractIdentifiedEnumAttributeConverter<AccessLevel> implements AttributeConverter<AccessLevel, Long> {

    public AccessLevelConverter() {
        super(AccessLevel.class);
    }

}
