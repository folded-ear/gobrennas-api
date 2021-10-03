package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToOne;

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
            CompoundQuantity quantity
    ) {
        super(item, quantity);
        this.priorQuantity = item.getQuantity().clone();
    }

    /**
     * The quantity before the transaction. As ` ResetTx` are destructive, it's
     * impossible to know the prior state without consulting other parts of the
     * history. This allows for that answer to be obtained directly.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = {CascadeType.ALL}
    )
    @Getter
    private CompoundQuantity priorQuantity;

    CompoundQuantity getCorrection() {
        return getNewQuantity().minus(priorQuantity);
    }

    public CompoundQuantity computeNewQuantity(CompoundQuantity curr) {
        return getQuantity().clone();
    }

    @Override
    public String toString() {
        return "ResetTx(quantity=" + getQuantity() + ", priorQuantity=" + priorQuantity + ", correction=" + getCorrection() + ")";
    }

}
