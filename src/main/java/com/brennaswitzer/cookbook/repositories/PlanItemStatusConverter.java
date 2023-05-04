package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlanItemStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class PlanItemStatusConverter extends AbstractIdentifiedEnumAttributeConverter<PlanItemStatus> implements AttributeConverter<PlanItemStatus, Long> {

    public PlanItemStatusConverter() {
        super(PlanItemStatus.class);
    }

}
