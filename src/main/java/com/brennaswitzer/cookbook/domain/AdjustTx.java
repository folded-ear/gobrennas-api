package com.brennaswitzer.cookbook.domain;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("4")
@NoArgsConstructor
public class AdjustTx extends InventoryTx {

    public AdjustTx(
            CompoundQuantity quantity
    ) {
        super(quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.plus(getQuantity());
    }

}
