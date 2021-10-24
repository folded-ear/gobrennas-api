package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "inventory_tx")
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class InventoryTx extends BaseEntity {

    @ManyToOne
    @Getter
    @ToString.Exclude
    private InventoryItem item;

    @Column(name = "dtype")
    @Getter
    private TxType type;

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
            TxType type,
            CompoundQuantity quantity
    ) {
        this.type = type;
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
        newQuantity = item.getQuantity().plus(getQuantity());
    }

    public CompoundQuantity getPriorQuantity() {
        return getNewQuantity().minus(getQuantity());
    }

}

