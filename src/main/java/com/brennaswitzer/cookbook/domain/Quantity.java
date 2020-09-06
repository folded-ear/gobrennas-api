package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.NumberUtils;

import javax.persistence.*;
import java.util.*;

@Embeddable
@Access(AccessType.FIELD)   // It is unclear why this is needed. The only JPA
                            // annotation at field or prop level is on a field.
                            // Clearly still something to learn here. :)
public class Quantity {

    public static final Quantity ZERO = count(0);
    public static final Quantity ONE = count(1);

    public static Quantity count(double count) {
        return new Quantity(count, null);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Column(nullable = true)
    private double quantity;

    @ManyToOne
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

    public boolean hasUnits() {
        return units != null;
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

    public Quantity plus(Quantity that) {
        if (Objects.equals(units, that.units)) {
            return new Quantity(quantity + that.quantity, units);
        }
        return plus(that.convertTo(units));
    }

    public Quantity times(Double factor) {
        if (factor.equals(1d)) return this;
        return new Quantity(quantity * factor, units);
    }

    @Override
    public String toString() {
        String qs = NumberUtils.formatNumber(quantity);
        return hasUnits()
                ? qs + " " + units.getName()
                : qs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Quantity)) return false;
        Quantity that = (Quantity) o;
        return Objects.equals(units, that.units)
                && Math.abs(quantity - that.quantity) < 0.001;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, units);
    }

}
