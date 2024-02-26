package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "inventory_tx")
@NoArgsConstructor
@ToString(includeFieldNames = false)
public class InventoryTx extends BaseEntity {

    @ManyToOne
    @Getter
    @ToString.Exclude
    private InventoryItem item;

    @Column(name = "dtype",
            columnDefinition = "int4")
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

