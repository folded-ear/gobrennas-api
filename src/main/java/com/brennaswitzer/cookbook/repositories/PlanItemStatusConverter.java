package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PlanItemStatusConverter extends AbstractIdentifiedEnumAttributeConverter<PlanItemStatus> {

    public PlanItemStatusConverter() {
        super(PlanItemStatus.class);
    }

}
