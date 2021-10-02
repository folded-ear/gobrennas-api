package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * You got some!
 */
@Entity
@DiscriminatorValue("1")
public class AcquireTx extends InventoryTx {

    public AcquireTx() {
    }

    public AcquireTx(
            InventoryItem item,
            CompoundQuantity quantity
    ) {
        super(item, quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.plus(getQuantity());
    }

}
