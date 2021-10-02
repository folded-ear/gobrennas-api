package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import javax.persistence.*;

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
    @Embedded
    @AssociationOverride(name = "components",
            joinTable = @JoinTable(name = "inventory_tx_prior_quantity"
                    , joinColumns = {@JoinColumn(name = "inventory_tx_id")}
            )
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
