package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Rating;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RatingConverter extends AbstractIdentifiedEnumAttributeConverter<Rating> implements AttributeConverter<Rating, Long> {

    public RatingConverter() {
        super(Rating.class);
    }

}
