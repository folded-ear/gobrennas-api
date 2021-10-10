package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_item")
public class InventoryItem extends BaseEntity {

    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @Getter
    @Setter
    private User user;

    @ManyToOne
    @Getter
    @Setter
    private PantryItem pantryItem;

    public InventoryItem() {
    }

    public InventoryItem(User user, PantryItem pantryItem) {
        this.user = user;
        this.pantryItem = pantryItem;
    }

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            orphanRemoval = true,
            mappedBy = "item",
            cascade = {CascadeType.ALL},
            fetch = FetchType.LAZY
    )
    @Getter
    private List<InventoryTx> transactions = new ArrayList<>();

    /**
     * This is a cache over {@link #transactions}.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            optional = false
    )
    @Getter
    private CompoundQuantity quantity = CompoundQuantity.ZERO;

    /**
     * This is a cache over {@link #transactions}.
     */
    @Getter
    private int txCount = 0;

    public InventoryTx acquire(Quantity quantity) {
        return acquire(new CompoundQuantity(quantity));
    }

    public InventoryTx acquire(CompoundQuantity quantity) {
        return addTransaction(new InventoryTx(TxType.ACQUIRE, quantity));
    }

    public InventoryTx consume(Quantity quantity) {
        return consume(new CompoundQuantity(quantity));
    }

    public InventoryTx consume(CompoundQuantity quantity) {
        quantity = quantity.negate();
        return addTransaction(new InventoryTx(TxType.CONSUME, quantity));
    }

    public InventoryTx discard(Quantity quantity) {
        return discard(new CompoundQuantity(quantity));
    }

    public InventoryTx discard(CompoundQuantity quantity) {
        quantity = quantity.negate();
        return addTransaction(new InventoryTx(TxType.DISCARD, quantity));
    }

    public InventoryTx adjust(Quantity quantity) {
        return reset(new CompoundQuantity(quantity));
    }

    public InventoryTx adjust(CompoundQuantity quantity) {
        return addTransaction(new InventoryTx(TxType.ADJUST, quantity));
    }

    public InventoryTx reset(Quantity quantity) {
        return reset(new CompoundQuantity(quantity));
    }

    public InventoryTx reset(CompoundQuantity quantity) {
        quantity = quantity.minus(this.getQuantity());
        return addTransaction(new InventoryTx(TxType.RESET, quantity));
    }

    private <T extends InventoryTx> T addTransaction(T tx) {
        tx.commit(this);
        transactions.add(tx);
        quantity = tx.getNewQuantity().clone();
        txCount = transactions.size();
        return tx;
    }

    @Override
    public String toString() {
        return "InventoryItem(pantryItem=" + pantryItem + ", available=" + quantity + ")";
    }

}
