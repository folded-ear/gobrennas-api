package com.brennaswitzer.cookbook.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

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
            CompoundQuantity quantity
    ) {
        super(item, quantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return curr.minus(getQuantity());
    }

}
