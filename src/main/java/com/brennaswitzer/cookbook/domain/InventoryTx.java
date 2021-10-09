package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity // @MappedSuperclass can't support a polymorphic @Repository
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER)
@NoArgsConstructor
public abstract class InventoryTx extends BaseEntity {

    @ManyToOne
    private InventoryItem item;

    /**
     * The transaction quantity.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            optional = false
    )
    @Getter
    private CompoundQuantity quantity;

    public InventoryTx(
            CompoundQuantity quantity
    ) {
        this.quantity = quantity.clone();
    }

    /**
     * The quantity after the transaction.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            optional = false
    )
    @Getter
    private CompoundQuantity newQuantity;

    final void commit(InventoryItem item) {
        this.item = item;
        newQuantity = computeNewQuantity(item.getQuantity());
    }

    abstract CompoundQuantity computeNewQuantity(CompoundQuantity curr);

    public CompoundQuantity getPriorQuantity() {
        return computeNewQuantity(getNewQuantity().negate()).negate();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(newQuantity=" + newQuantity + ", quantity=" + quantity + ")";
    }

}

