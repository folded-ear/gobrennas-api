package com.brennaswitzer.cookbook.domain.measure;

import java.util.*;

public class Quantity {

    private double quantity;

    private UnitOfMeasure units;

    public Quantity() {
    }

    public Quantity(double quantity, UnitOfMeasure units) {
        this.quantity = quantity;
        this.units = units;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public UnitOfMeasure getUnits() {
        return units;
    }

    public void setUnits(UnitOfMeasure units) {
        this.units = units;
    }

    public Quantity convertTo(UnitOfMeasure uom) {
        // this isn't really needed, but it'll save some allocation
        if (units.hasConversion(uom)) {
            return new Quantity(
                    quantity * units.getConversion(uom),
                    uom);
        }
        // no direct conversion; start walking
        Queue<Quantity> queue = new LinkedList<>();
        Set<UnitOfMeasure> visited = new HashSet<>();
        queue.add(this);
        while (!queue.isEmpty()) {
            Quantity q = queue.remove();
            if (!visited.add(q.units)) continue;
            if (q.units.equals(uom)) return q;
            for (Map.Entry<UnitOfMeasure, Double> e : q.units.getConversions().entrySet()) {
                if (visited.contains(e.getKey())) continue;
                queue.add(new Quantity(
                        q.quantity * e.getValue(),
                        e.getKey()));
            }
        }
        throw new NoConversionException(units, uom);
    }

    @Override
    public String toString() {
        return quantity + " " + units.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity)) return false;
        Quantity that = (Quantity) o;
        return units.equals(that.units)
                && Math.abs(quantity - that.quantity) < 0.001;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, units);
    }
}
