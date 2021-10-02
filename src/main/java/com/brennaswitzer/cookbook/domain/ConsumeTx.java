package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * You used some!
 */
@Entity
@DiscriminatorValue("2")
public class ConsumeTx extends InventoryTx {

    public ConsumeTx() {
    }

    public ConsumeTx(
            InventoryItem item,
            CompoundQuantity quantity
    ) {
        super(item, quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.minus(getQuantity());
    }

}
