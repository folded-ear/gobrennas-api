package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class CompoundQuantity implements Cloneable {

    public static final CompoundQuantity ZERO = new CompoundQuantity();

    public static final CompoundQuantity ONE = new CompoundQuantity(Quantity.ONE);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    @ElementCollection
    @Getter
    private Collection<Quantity> components;

    public CompoundQuantity() {
        components = Collections.emptyList();
    }

    public CompoundQuantity(Quantity component) {
        this(Collections.singleton(component));
    }

    public CompoundQuantity(Collection<Quantity> components) {
        this.components = new ArrayList<>(components);
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    private static void addToUnitMap(Map<UnitOfMeasure, Quantity> byUnit, Quantity q) {
        var units = q.getUnits();
        if (byUnit.containsKey(units)) {
            byUnit.put(units, byUnit.get(units).plus(q));
            return;
        }
        Quantity converted = null;
        for (val u : byUnit.keySet()) {
            try {
                converted = q.convertTo(u);
                break;
            } catch (NoConversionException ignored) {
            }
        }
        if (converted == null) {
            byUnit.put(units, q);
        } else {
            units = converted.getUnits();
            byUnit.put(units, byUnit.get(units).plus(converted));
        }
    }

    private static void addAllToUnitMap(Map<UnitOfMeasure, Quantity> byUnit, Collection<Quantity> more) {
        for (val q : more) {
            addToUnitMap(byUnit, q);
        }
    }

    private static void addAllToUnitMap(Map<UnitOfMeasure, Quantity> byUnit, Map<UnitOfMeasure, Quantity> more) {
        addAllToUnitMap(byUnit, more.values());
    }

    public CompoundQuantity plus(CompoundQuantity other) {
        if (other.isEmpty()) return clone();
        if (isEmpty()) return other.clone();
        val byUnit = components.stream().collect(
                HashMap::new,
                CompoundQuantity::addToUnitMap,
                CompoundQuantity::addAllToUnitMap
        );
        addAllToUnitMap(byUnit, other.components);
        return new CompoundQuantity(byUnit.values());
    }

    public CompoundQuantity minus(CompoundQuantity other) {
        if (other.isEmpty()) return clone();
        return plus(other.getInverse());
    }

    public CompoundQuantity getInverse() {
        Collection<Quantity> inverse = new HashSet<>();
        for (Quantity q : components) {
            inverse.add(new Quantity(-q.getQuantity(), q.getUnits()));
        }
        return new CompoundQuantity(inverse);
    }

    @Override
    public CompoundQuantity clone() {
        try {
            CompoundQuantity clone = (CompoundQuantity) super.clone();
            clone.components = new ArrayList<>(components);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompoundQuantity)) return false;

        CompoundQuantity that = (CompoundQuantity) o;

        return Objects.equals(components, that.components);
    }

    @Override
    public int hashCode() {
        return components != null ? components.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CompoundQuantity(" + components.stream()
                .map(Quantity::toString)
                .collect(Collectors.joining(", ")) + ")";
    }

}
