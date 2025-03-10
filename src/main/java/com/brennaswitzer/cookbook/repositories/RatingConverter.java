package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Rating;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RatingConverter extends AbstractIdentifiedEnumAttributeConverter<Rating> {

    public RatingConverter() {
        super(Rating.class);
    }

}
