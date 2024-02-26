package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PlanItemStatusConverter extends AbstractIdentifiedEnumAttributeConverter<PlanItemStatus> implements AttributeConverter<PlanItemStatus, Long> {

    public PlanItemStatusConverter() {
        super(PlanItemStatus.class);
    }

}
