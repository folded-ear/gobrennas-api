package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Set;

/**
 * You threw some away!
 */
@Entity
@DiscriminatorValue("3")
public class DiscardTx extends InventoryTx {

    public DiscardTx() {
    }

    public DiscardTx(
            InventoryItem item,
            Set<Quantity> quantity
    ) {
        super(item, quantity);
    }

    public Set<Quantity> computeNewQuantity(Set<Quantity> curr) {
        return Quantity.minus(curr, getQuantity());
    }
}
