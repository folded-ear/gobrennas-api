package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.util.ValueUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class IngredientRefInfo {

    @Getter
    @Setter
    private String raw;
    @Getter
    @Setter
    private Double quantity;
    private String units;
    @Getter
    @Setter
    private Long uomId;
    @Getter
    @Setter
    private String ingredient;
    @Getter
    @Setter
    private Long ingredientId;
    @Getter
    @Setter
    private String preparation;

    public boolean hasQuantity() {
        return quantity != null;
    }

    @Deprecated
    public String getUnits() {
        return units;
    }

    @Deprecated
    public void setUnits(String units) {
        this.units = units;
    }

    @Deprecated
    public boolean hasUnits() {
        return ValueUtils.hasValue(units);
    }

    public boolean hasUomId() {
        return uomId != null;
    }

    public boolean hasIngredient() {
        return this.ingredient != null;
    }

    public boolean hasIngredientId() {
        return this.ingredientId != null;
    }

    public Quantity extractQuantity(EntityManager em) {
        UnitOfMeasure uom = hasUomId()
                ? em.find(UnitOfMeasure.class, getUomId())
                : hasUnits()
                ? UnitOfMeasure.ensure(em, getUnits())
                : null;
        return new Quantity(getQuantity(), uom);
    }

    public static IngredientRefInfo from(IngredientRef ref) {
        IngredientRefInfo info = new IngredientRefInfo();
        info.setRaw(ref.getRaw());
        if (ref.hasQuantity()) {
            Quantity q = ref.getQuantity();
            info.setQuantity(q.getQuantity());
            if (q.hasUnits()) {
                info.setUomId(q.getUnits().getId());
                info.setUnits(q.getUnits().getName());
            }
        }
        if (ref.hasIngredient()) {
            info.setIngredientId(ref.getIngredient().getId());
            info.setIngredient(ref.getIngredient().getName());
        }
        info.setPreparation(ref.getPreparation());
        return info;
    }

}
