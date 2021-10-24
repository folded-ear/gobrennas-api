package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;

@Getter
@Setter
public class QuantityInfo {

    private Double quantity;
    private String units;
    private Long uomId;

    public boolean hasUnits() {
        return units != null && !"".equals(units) && !units.trim().isEmpty();
    }

    public boolean hasUomId() {
        return uomId != null;
    }

    public Quantity extractQuantity(EntityManager em) {
        UnitOfMeasure uom = hasUomId()
                ? em.find(UnitOfMeasure.class, getUomId())
                : hasUnits()
                ? UnitOfMeasure.ensure(em, getUnits())
                : null;
        return new Quantity(getQuantity(), uom);
    }

}
