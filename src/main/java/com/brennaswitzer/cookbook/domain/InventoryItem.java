package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_item")
public class InventoryItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Ingredient ingredient;

    public InventoryItem() {
    }

    public InventoryItem(User user, Ingredient ingredient) {
        this.user = user;
        this.ingredient = ingredient;
    }

    @SuppressWarnings("FieldMayBeFinal")
    @OneToMany(
            orphanRemoval = true,
            mappedBy = "item",
            cascade = { CascadeType.ALL },
            fetch = FetchType.LAZY)
    @Getter
    private List<InventoryTx> transactions = new ArrayList<>();

    /**
     * This is a cache over {@link #transactions}.
     */
    @OneToOne(
            orphanRemoval = true,
            cascade = { CascadeType.ALL },
            optional = false)
    @Getter
    private CompoundQuantity quantity = CompoundQuantity.zero();

    /**
     * This is a cache over {@link #transactions}.
     */
    @Getter
    private int txCount = 0;

    public InventoryTx transaction(TxType type, Quantity quantity) {
        return transaction(type, new CompoundQuantity(quantity));
    }

    public InventoryTx transaction(TxType type, CompoundQuantity quantity) {
        switch (type) {
            case ACQUIRE:
                return acquire(quantity);
            case CONSUME:
                return consume(quantity);
            case DISCARD:
                return discard(quantity);
            case ADJUST:
                return adjust(quantity);
            case RESET:
                return reset(quantity);
        }
        throw new IllegalArgumentException(String.format("Unknown %s transaction type", type));
    }

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
        return "InventoryItem(ingredient=" + ingredient + ", available=" + quantity + ")";
    }

}
