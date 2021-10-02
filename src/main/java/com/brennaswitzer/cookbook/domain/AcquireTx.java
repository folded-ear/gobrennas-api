package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

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
            Set<Quantity> quantity
    ) {
        super(item, quantity);
    }

    public Set<Quantity> computeNewQuantity(Set<Quantity> curr) {
        return Quantity.plus(curr, getQuantity());
    }
}
