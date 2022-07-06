package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.hibernate.annotations.SortComparator;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class CompoundQuantity implements Cloneable, Identified {

    public static CompoundQuantity zero() {
        return new CompoundQuantity();
    }

    public static CompoundQuantity one() {
        return new CompoundQuantity(Quantity.ONE);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_seq")
    @Getter
    @Setter
    private Long id;

    @ElementCollection
    @SortComparator(Quantity.ByUnitAndQuantityComparator.class)
    @Getter
    private SortedSet<Quantity> components;

    public CompoundQuantity() {
        components = Collections.emptySortedSet();
    }

    public CompoundQuantity(Quantity component) {
        this(Collections.singletonList(component));
    }

    public CompoundQuantity(Quantity... components) {
        this(Arrays.asList(components));
    }

    public CompoundQuantity(Collection<Quantity> components) {
        this.components = components
                .stream()
                .filter(q -> q.getQuantity() != 0)
                .sorted(new Quantity.ByUnitAndQuantityComparator())
                .collect(Collectors.toCollection(CompoundQuantity::constructComponentsSet));
    }

    private static SortedSet<Quantity> constructComponentsSet() {
        return new TreeSet<>(new Quantity.ByUnitAndQuantityComparator());
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

    public CompoundQuantity plus(Quantity other) {
        return plus(new CompoundQuantity(other));
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

    public CompoundQuantity minus(Quantity other) {
        return minus(new CompoundQuantity(other));
    }

    public CompoundQuantity minus(CompoundQuantity other) {
        if (other.isEmpty()) return clone();
        return plus(other.negate());
    }

    public CompoundQuantity negate() {
        return new CompoundQuantity(components.stream()
                .map(q -> new Quantity(-q.getQuantity(), q.getUnits()))
                .collect(Collectors.toList()));
    }

    @Override
    public CompoundQuantity clone() {
        try {
            CompoundQuantity clone = (CompoundQuantity) super.clone();
            clone.components = constructComponentsSet();
            clone.components.addAll(components);
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
