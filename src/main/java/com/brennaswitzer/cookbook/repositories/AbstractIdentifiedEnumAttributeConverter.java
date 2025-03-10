package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Identified;
import jakarta.persistence.AttributeConverter;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConverterNotAnnotatedInspection")
abstract class AbstractIdentifiedEnumAttributeConverter<E extends Enum<E> & Identified> implements AttributeConverter<E, Long> {

    private final Class<E> clazz;
    private final Map<Long, E> byId;

    AbstractIdentifiedEnumAttributeConverter(Class<E> clazz) {
        this.clazz = clazz;
        this.byId = new HashMap<>();
        for (var v : clazz.getEnumConstants())
            byId.put(v.getId(), v);
    }

    public Long convertToDatabaseColumn(E attribute) {
        if (attribute == null) return null;
        return attribute.getId();
    }

    public E convertToEntityAttribute(Long id) {
        if (id == null) return null;
        var v = byId.get(id);
        if (v != null) return v;
        throw new IllegalArgumentException(String.format(
                "No '%s' with id '%s' is known.",
                        clazz.getSimpleName(),
                id));
    }

}
