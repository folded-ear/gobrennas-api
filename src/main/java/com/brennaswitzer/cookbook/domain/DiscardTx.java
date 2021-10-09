package com.brennaswitzer.cookbook.domain;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("3")
@NoArgsConstructor
public class DiscardTx extends InventoryTx {

    public DiscardTx(
            CompoundQuantity quantity
    ) {
        super(quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.minus(getQuantity());
    }

}
