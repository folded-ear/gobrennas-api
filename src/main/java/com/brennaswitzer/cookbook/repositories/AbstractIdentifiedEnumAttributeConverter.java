package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Identified;

import java.util.stream.Stream;

abstract class AbstractIdentifiedEnumAttributeConverter<E extends Enum<E> & Identified> {

    private final Class<E> clazz;

    AbstractIdentifiedEnumAttributeConverter(Class<E> clazz) {
        this.clazz = clazz;
    }

    public Long convertToDatabaseColumn(E attribute) {
        if (attribute == null) return null;
        return attribute.getId();
    }

    public E convertToEntityAttribute(Long id) {
        if (id == null) return null;
        return Stream.of(clazz.getEnumConstants())
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
