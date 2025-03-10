package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.DataType;
import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Arrays;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AbstractIdentifiedEnumAttributeConverterTest {

    @ParameterizedTest
    @EnumSource(PlanItemStatus.class)
    void toDatabase(PlanItemStatus v) {
        var converter = new AbstractIdentifiedEnumAttributeConverter<>(PlanItemStatus.class) {};

        assertEquals(v.getId(), converter.convertToDatabaseColumn(v));
    }

    @ParameterizedTest
    @EnumSource(AccessLevel.class)
    void toAttribute(AccessLevel v) {
        var converter = new AbstractIdentifiedEnumAttributeConverter<>(AccessLevel.class) {};

        assertEquals(v, converter.convertToEntityAttribute(v.getId()));
    }

    @Test
    void unknownId() {
        var converter = new AbstractIdentifiedEnumAttributeConverter<>(DataType.class) {};

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        var maxId = Arrays.stream(DataType.class.getEnumConstants())
                .map(Identified::getId)
                .max(Comparator.naturalOrder())
                .get();
        assertThrows(IllegalArgumentException.class,
                     () -> converter.convertToEntityAttribute(maxId + 1));
    }

}
