package com.brennaswitzer.cookbook.domain.measure;

import com.brennaswitzer.cookbook.util.IdUtils;

import java.util.*;

public class UnitOfMeasure {

    private Long id = IdUtils.next(getClass());

    private String name;

    private Set<String> aliases;

    private Map<UnitOfMeasure, Float> conversions = new HashMap<>();

    public UnitOfMeasure() {
    }

    public UnitOfMeasure(String name, String... aliases) {
        setName(name);
        addAliases(aliases);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasAlias(String alias) {
        if (aliases == null) return false;
        return aliases.contains(alias);
    }

    public boolean addAlias(String alias) {
        if (aliases == null) aliases = new HashSet<>();
        return aliases.add(alias);
    }

    public void addAliases(String... aliases) {
        for (String a : aliases) addAlias(a);
    }

    public boolean removeAlias(String alias) {
        if (aliases == null) return false;
        return aliases.remove(alias);
    }

    public Set<String> getAliases() {
        if (aliases == null) return Collections.EMPTY_SET;
        return Collections.unmodifiableSet(aliases);
    }

    public boolean hasConversion(UnitOfMeasure uom) {
        return conversions.containsKey(uom);
    }

    public Float getConversion(UnitOfMeasure uom) {
        return conversions.get(uom);
    }

    public Float addConversion(UnitOfMeasure uom, Float factor) {
        uom.conversions.put(this, 1f / factor);
        return conversions.put(uom, factor);
    }

    public Float removeConversion(UnitOfMeasure uom) {
        return conversions.remove(uom);
    }

    public Map<UnitOfMeasure, Float> getConversions() {
        return Collections.unmodifiableMap(conversions);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitOfMeasure)) return false;
        UnitOfMeasure that = (UnitOfMeasure) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
