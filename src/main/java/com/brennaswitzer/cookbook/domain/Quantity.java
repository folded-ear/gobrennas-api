package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.NumberUtils;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Embeddable
@Access(AccessType.FIELD)   // It is unclear why this is needed. The only JPA
// annotation at field or prop level is on a field.
// Clearly still something to learn here. :)
public class Quantity {

    // stupid IntelliJ / JPA Buddy
    @SuppressWarnings("EmbeddedNotMarkedInspection")
    public static final Quantity ZERO = count(0);

    // stupid IntelliJ / JPA Buddy
    @SuppressWarnings("EmbeddedNotMarkedInspection")
    public static final Quantity ONE = count(1);

    public static Quantity count(Number count) {
        return new Quantity(count, null);
    }

    @SuppressWarnings("DefaultAnnotationParam")
    @Column(nullable = true)
    @Getter
    @Setter
    private double quantity;

    @ManyToOne
    @Getter
    @Setter
    private UnitOfMeasure units;

    public Quantity() {
    }

    public Quantity(Number quantity, UnitOfMeasure units) {
        this.quantity = quantity.doubleValue();
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
            if (q.units.equals(uom)) {
                // write it down for later
                units.addConversion(q.units, q.quantity / quantity);
                return q;
            }
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
        if (0d == that.quantity) return this;
        if (0d == this.quantity) return that;
        if (Objects.equals(units, that.units)) {
            return new Quantity(quantity + that.quantity, units);
        }
        return plus(that.convertTo(units));
    }

    public Quantity minus(Quantity that) {
        return plus(new Quantity(-that.quantity, that.units));
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
