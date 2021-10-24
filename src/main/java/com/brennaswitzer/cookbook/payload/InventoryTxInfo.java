package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.CompoundQuantity;
import com.brennaswitzer.cookbook.domain.TxType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
public class InventoryTxInfo {
    private Long id;
    private TxType type;
    private Instant createdAt;
    private Collection<QuantityInfo> quantity;
    private Collection<QuantityInfo> newQuantity;
    private String ingredient;
    private Long ingredientId;

    public boolean hasIngredient() {
        return this.ingredient != null;
    }

    public boolean hasIngredientId() {
        return this.ingredientId != null;
    }

    public CompoundQuantity extractQuantity(EntityManager em) {
        return new CompoundQuantity(quantity.stream()
                .map(q -> q.extractQuantity(em))
                .collect(Collectors.toList()));
    }
}
