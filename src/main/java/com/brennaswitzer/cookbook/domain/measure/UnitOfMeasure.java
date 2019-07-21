package com.brennaswitzer.cookbook.domain.measure;

import com.brennaswitzer.cookbook.util.IdUtils;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.util.*;

@Entity
public class UnitOfMeasure {

    @Id
    private Long id = IdUtils.next(getClass());

    private String name;

    private String pluralName;

    @ElementCollection
    @Column(name = "alias")
    private Set<String> aliases;

    @ElementCollection
    @MapKeyJoinColumn(name = "target_id")
    @Column(name = "factor")
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
        Assert.notNull(name, "Can't have the null UoM");
        this.name = name;
    }

    public String getPluralName() {
        return pluralName;
    }

    public void setPluralName(String pluralName) {
        this.pluralName = pluralName;
    }

    public boolean hasAlias(String alias) {
        if (aliases == null) return false;
        return aliases.contains(alias);
    }

    public boolean addAlias(String alias) {
        Assert.notNull(alias, "Can't alias null");
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
        Assert.notNull(uom, "Can't convert to the null UoM");
        Assert.notNull(factor, "UoM conversion factor's can't be null");
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
