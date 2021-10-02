package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity // @MappedSuperclass can't support a polymorphic @Repository
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
public abstract class InventoryTx extends BaseEntity {

    @ManyToOne
    private InventoryItem item;

    /**
     * The quantity after the transaction.
     */
    @Embedded
    @AssociationOverride(name = "components",
            joinTable = @JoinTable(name = "inventory_tx_quantity"))
    @Getter
    private CompoundQuantity quantity;

    public InventoryTx() {
    }

    public InventoryTx(
            InventoryItem item,
            CompoundQuantity quantity
    ) {
        this.item = item;
        this.quantity = quantity.clone();
    }

    /**
     * The quantity after the transaction.
     */
    @Embedded
    @AssociationOverride(name = "components",
            joinTable = @JoinTable(name = "inventory_tx_new_quantity"))
    @Getter
    private CompoundQuantity newQuantity;

    final void commit() {
        newQuantity = computeNewQuantity(item.getQuantity());
    }

    abstract CompoundQuantity computeNewQuantity(CompoundQuantity curr);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(quantity=" + quantity + ", newQuantity=" + newQuantity + ")";
    }

}

