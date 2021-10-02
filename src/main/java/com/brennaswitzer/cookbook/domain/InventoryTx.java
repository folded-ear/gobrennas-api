package com.brennaswitzer.cookbook.domain;

import lombok.Getter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity // @MappedSuperclass can't support a polymorphic @Repository
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
public abstract class InventoryTx extends BaseEntity {

    @ManyToOne
    private InventoryItem item;

    /**
     * The quantity after the transaction.
     */
    @ElementCollection
    @Getter
    private Set<Quantity> quantity = new HashSet<>();

    public InventoryTx() {
    }

    public InventoryTx(
            InventoryItem item,
            Set<Quantity> quantity
    ) {
        this.item = item;
        this.quantity = quantity;
    }

    /**
     * The quantity after the transaction.
     */
    @ElementCollection
    @Getter
    private Set<Quantity> newQuantity;

    final void commit() {
        newQuantity = computeNewQuantity(item.getQuantity());
    }

    abstract Set<Quantity> computeNewQuantity(Set<Quantity> curr);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(quantity=" + quantity + ", newQuantity=" + newQuantity + ")";
    }

}

