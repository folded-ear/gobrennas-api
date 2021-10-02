package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

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
            Set<Quantity> quantity
    ) {
        super(item, quantity);
    }

    public Set<Quantity> computeNewQuantity(Set<Quantity> curr) {
        return Quantity.minus(curr, getQuantity());
    }
}
