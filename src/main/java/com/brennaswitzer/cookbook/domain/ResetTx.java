package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.Set;

/**
 * You lost track and need a reset!
 */
@Entity
@DiscriminatorValue("4")
public class ResetTx extends InventoryTx {

    public ResetTx() {
    }

    public ResetTx(
            InventoryItem item,
            Set<Quantity> quantity
    ) {
        super(item, quantity);
        this.priorQuantity = item.getQuantity();
    }

    /**
     * The quantity before the transaction. As ` ResetTx` are destructive, it's
     * impossible to know the prior state without consulting other parts of the
     * history. This allows for that answer to be obtained directly.
     */
    @ElementCollection
    @Getter
    private Set<Quantity> priorQuantity;

    Set<Quantity> getCorrection() {
        return Quantity.minus(getNewQuantity(), priorQuantity);
    }

    public Set<Quantity> computeNewQuantity(Set<Quantity> curr) {
        return getQuantity();
    }

    @Override
    public String toString() {
        return "ResetTx(quantity=" + getQuantity() + ", priorQuantity=" + priorQuantity + ", correction=" + getCorrection() + ")";
    }

}
