package com.brennaswitzer.cookbook.domain;

import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InventoryItemTest {
    private final CompoundQuantity neg25 = new CompoundQuantity(Quantity.count(-25));
    private final CompoundQuantity neg17 = new CompoundQuantity(Quantity.count(-17));
    private final CompoundQuantity zero = new CompoundQuantity(Quantity.count(0));
    private final CompoundQuantity pos17 = new CompoundQuantity(Quantity.count(17));
    private final CompoundQuantity pos25 = new CompoundQuantity(Quantity.count(25));
    private final CompoundQuantity pos42 = new CompoundQuantity(Quantity.count(42));

    @Test
    public void acquire() {
        val item = new InventoryItem();

        val a = item.acquire(pos17);
        assertEquals(TxType.ACQUIRE, a.getType());
        assertEquals(a.getQuantity(), pos17);
        assertEquals(a.getNewQuantity(), pos17);
        assertEquals(a.getPriorQuantity(), zero);
        assertEquals(item.getQuantity(), pos17);
        assertEquals(1, item.getTxCount());

        val b = item.acquire(pos25);
        assertEquals(b.getQuantity(), pos25);
        assertEquals(b.getNewQuantity(), pos42);
        assertEquals(b.getPriorQuantity(), pos17);
        assertEquals(item.getQuantity(), pos42);
        assertEquals(2, item.getTxCount());
    }

    @Test
    public void consume() {
        val item = new InventoryItem();
        item.reset(pos42);

        val a = item.consume(pos25);
        assertEquals(TxType.CONSUME, a.getType());
        assertEquals(a.getQuantity(), neg25);
        assertEquals(a.getNewQuantity(), pos17);
        assertEquals(a.getPriorQuantity(), pos42);
        assertEquals(item.getQuantity(), pos17);
        assertEquals(2, item.getTxCount());

        val b = item.consume(pos17);
        assertEquals(b.getQuantity(), neg17);
        assertEquals(b.getNewQuantity(), zero);
        assertEquals(b.getPriorQuantity(), pos17);
        assertEquals(item.getQuantity(), zero);
        assertEquals(3, item.getTxCount());
    }

    @Test
    public void discard() {
        val item = new InventoryItem();
        item.reset(pos42);

        val a = item.discard(pos25);
        assertEquals(TxType.DISCARD, a.getType());
        assertEquals(a.getQuantity(), neg25);
        assertEquals(a.getNewQuantity(), pos17);
        assertEquals(a.getPriorQuantity(), pos42);
        assertEquals(item.getQuantity(), pos17);
        assertEquals(2, item.getTxCount());

        val b = item.discard(pos17);
        assertEquals(b.getQuantity(), neg17);
        assertEquals(b.getNewQuantity(), zero);
        assertEquals(b.getPriorQuantity(), pos17);
        assertEquals(item.getQuantity(), zero);
        assertEquals(3, item.getTxCount());
    }

    @Test
    public void adjust() {
        val item = new InventoryItem();

        val a = item.adjust(pos42);
        assertEquals(TxType.ADJUST, a.getType());
        assertEquals(a.getQuantity(), pos42);
        assertEquals(a.getNewQuantity(), pos42);
        assertEquals(a.getPriorQuantity(), zero);
        assertEquals(item.getQuantity(), pos42);
        assertEquals(1, item.getTxCount());

        val b = item.adjust(neg25);
        assertEquals(b.getQuantity(), neg25);
        assertEquals(b.getNewQuantity(), pos17);
        assertEquals(b.getPriorQuantity(), pos42);
        assertEquals(item.getQuantity(), pos17);
        assertEquals(2, item.getTxCount());
    }

    @Test
    public void reset() {
        val item = new InventoryItem();

        val a = item.reset(pos42);
        assertEquals(TxType.RESET, a.getType());
        assertEquals(a.getQuantity(), pos42);
        assertEquals(a.getNewQuantity(), pos42);
        assertEquals(a.getPriorQuantity(), zero);
        assertEquals(item.getQuantity(), pos42);
        assertEquals(1, item.getTxCount());

        val b = item.reset(pos17);
        assertEquals(b.getQuantity(), neg25);
        assertEquals(b.getNewQuantity(), pos17);
        assertEquals(b.getPriorQuantity(), pos42);
        assertEquals(item.getQuantity(), pos17);
        assertEquals(2, item.getTxCount());
    }

    @Test
    public void resetPizza() {
        UnitOfMeasure oz = new UnitOfMeasure("oz");
        UnitOfMeasure tbsp = new UnitOfMeasure("Tbsp");
        UnitOfMeasure tsp = new UnitOfMeasure("tsp");
        UnitOfMeasure lbs = new UnitOfMeasure("lbs");
        val item = new InventoryItem();

        item.acquire(new Quantity(1, oz));
        assertEquals(item.getQuantity(), new CompoundQuantity(
                new Quantity(1, oz)
        ));
        item.acquire(new Quantity(2, tbsp));
        assertEquals(item.getQuantity(), new CompoundQuantity(
                new Quantity(1, oz),
                new Quantity(2, tbsp)
        ));
        item.acquire(new Quantity(42, tsp));
        assertEquals(item.getQuantity(), new CompoundQuantity(
                new Quantity(1, oz),
                new Quantity(2, tbsp),
                new Quantity(42, tsp)
        ));
        item.acquire(new Quantity(3, tsp));
        item.reset(new Quantity(2.5, lbs));
        assertEquals(item.getQuantity(), new CompoundQuantity(
                new Quantity(2.5, lbs)
        ));

        System.out.println("===============================================================");
        for (val tx : item.getTransactions()) {
            System.out.println(tx);
        }
        System.out.println("===============================================================");
        System.out.println(item.getQuantity());
        System.out.println("===============================================================");
    }

}
