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
            cascade = {CascadeType.ALL}
    )
    @Getter
    private CompoundQuantity quantity = CompoundQuantity.ZERO;

    /**
     * This is a cache over @{link {@link #transactions}.
     */
    @Getter
    private int txCount = 0;

    public void acquire(Quantity quantity) {
        acquire(new CompoundQuantity(quantity));
    }

    public void acquire(CompoundQuantity quantity) {
        addTransaction(
                new AcquireTx(
                        this,
                        quantity
                )
        );
    }

    public void consume(Quantity quantity) {
        consume(new CompoundQuantity(quantity));
    }

    public void consume(CompoundQuantity quantity) {
        addTransaction(
                new ConsumeTx(
                        this,
                        quantity
                )
        );
    }

    public void discard(Quantity quantity) {
        discard(new CompoundQuantity(quantity));
    }

    public void discard(CompoundQuantity quantity) {
        addTransaction(
                new DiscardTx(
                        this,
                        quantity
                )
        );
    }

    public void reset(Quantity quantity) {
        reset(new CompoundQuantity(quantity));
    }

    public void reset(CompoundQuantity quantity) {
        addTransaction(
                new ResetTx(
                        this,
                        quantity
                )
        );
    }

    private void addTransaction(InventoryTx tx) {
        transactions.add(tx);
        tx.commit();
        quantity = tx.getNewQuantity().clone();
        txCount = transactions.size();
    }

    @Override
    public String toString() {
        return "InventoryItem(pantryItem=" + pantryItem + ", available=" + quantity + ")";
    }

}
