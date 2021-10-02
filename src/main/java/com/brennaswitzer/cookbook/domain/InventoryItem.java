package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

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
    @ElementCollection
    @Getter
    private Set<Quantity> quantity = new HashSet<>();

    /**
     * This is a cache over @{link {@link #transactions}.
     */
    @Getter
    private int txCount = 0;

    public void acquire(Quantity quantity) {
        acquire(Collections.singleton(quantity));
    }

    public void acquire(Set<Quantity> quantity) {
        addTransaction(
                new AcquireTx(
                        this,
                        quantity
                )
        );
    }

    public void consume(Quantity quantity) {
        consume(Collections.singleton(quantity));
    }

    public void consume(Set<Quantity> quantity) {
        addTransaction(
                new ConsumeTx(
                        this,
                        quantity
                )
        );
    }

    public void discard(Quantity quantity) {
        discard(Collections.singleton(quantity));
    }

    public void discard(Set<Quantity> quantity) {
        addTransaction(
                new DiscardTx(
                        this,
                        quantity
                )
        );
    }

    public void reset(Quantity quantity) {
        reset(Collections.singleton(quantity));
    }

    public void reset(Set<Quantity> quantity) {
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
        quantity = tx.getNewQuantity();
        txCount += 1;
    }

    @Override
    public String toString() {
        return "InventoryItem(pantryItem=" + pantryItem + ", available=" + quantity + ")";
    }

}
