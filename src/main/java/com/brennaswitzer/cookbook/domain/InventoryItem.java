package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
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
     * This is a cache over @{link {@link #transactions}.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = {CascadeType.ALL},
            optional = false
    )
    @Getter
    private CompoundQuantity quantity = CompoundQuantity.ZERO;

    /**
     * This is a cache over @{link {@link #transactions}.
     */
    @Getter
    private int txCount = 0;

    public AcquireTx acquire(Quantity quantity) {
        return acquire(new CompoundQuantity(quantity));
    }

    public AcquireTx acquire(CompoundQuantity quantity) {
        return addTransaction(new AcquireTx(quantity));
    }

    public ConsumeTx consume(Quantity quantity) {
        return consume(new CompoundQuantity(quantity));
    }

    public ConsumeTx consume(CompoundQuantity quantity) {
        return addTransaction(new ConsumeTx(quantity));
    }

    public DiscardTx discard(Quantity quantity) {
        return discard(new CompoundQuantity(quantity));
    }

    public DiscardTx discard(CompoundQuantity quantity) {
        return addTransaction(new DiscardTx(quantity));
    }

    public AdjustTx adjust(Quantity quantity) {
        return reset(new CompoundQuantity(quantity));
    }

    public AdjustTx adjust(CompoundQuantity quantity) {
        return addTransaction(new AdjustTx(quantity));
    }

    public AdjustTx reset(Quantity quantity) {
        return reset(new CompoundQuantity(quantity));
    }

    public AdjustTx reset(CompoundQuantity quantity) {
        CompoundQuantity adjustment = quantity.minus(this.getQuantity());
        return addTransaction(new AdjustTx(adjustment));
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
