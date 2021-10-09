package com.brennaswitzer.cookbook.domain;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("1")
@NoArgsConstructor
public class AcquireTx extends InventoryTx {

    public AcquireTx(
            CompoundQuantity quantity
    ) {
        super(quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.plus(getQuantity());
    }

}
