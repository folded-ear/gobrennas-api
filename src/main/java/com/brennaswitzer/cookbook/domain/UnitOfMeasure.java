package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.EnglishUtils;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.MapKeyJoinColumn;
import jakarta.persistence.NamedQuery;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity
@NamedQuery(
        name = "UnitOfMeasure.byName",
        query = """
                select uom
                from UnitOfMeasure uom
                    left join uom.aliases a
                where uom.name = :name
                    or uom.pluralName = :name
                    or a = :name
                order by case :name when uom.name then 1
                when uom.pluralName then 2
                else 3
                end""")
public class UnitOfMeasure extends BaseEntity {

    public static final Comparator<UnitOfMeasure> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.name.compareTo(b.name);
    };

    public static Optional<UnitOfMeasure> find(EntityManager entityManager, String name) {
        if (name == null) return Optional.empty();
        name = EnglishUtils.unpluralize(name.trim());
        List<UnitOfMeasure> uoms = entityManager.createNamedQuery(
                        "UnitOfMeasure.byName",
                        UnitOfMeasure.class
                )
                .setParameter("name", name)
                .getResultList();
        if (!uoms.isEmpty()) return Optional.of(uoms.get(0));
        // fine. try lowercased
        uoms = entityManager.createNamedQuery(
                        "UnitOfMeasure.byName",
                        UnitOfMeasure.class
                )
                .setParameter("name", name.toLowerCase())
                .getResultList();
        if (!uoms.isEmpty()) return Optional.of(uoms.get(0));
        return Optional.empty();
    }

    public static UnitOfMeasure ensure(EntityManager entityManager, String name) {
        if (name == null) throw new NullPointerException();
        return find(entityManager, name)
                .orElseGet(() -> {
                    UnitOfMeasure uom = new UnitOfMeasure(
                            EnglishUtils.unpluralize(name.trim()));
                    entityManager.persist(uom);
                    return uom;
                });
    }

    @NonNull
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String pluralName;

    @ElementCollection
    @Column(name = "alias")
    private Set<String> aliases;

    @ElementCollection
    @MapKeyJoinColumn(name = "target_id")
    @Column(name = "factor")
    private Map<UnitOfMeasure, Double> conversions = new HashMap<>();

    public UnitOfMeasure() {
    }

    public UnitOfMeasure(String name, String... aliases) {
        setName(name);
        addAliases(aliases);
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
        if (aliases == null) {
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(aliases);
    }

    public boolean hasConversion(UnitOfMeasure uom) {
        return conversions.containsKey(uom);
    }

    public Double getConversion(UnitOfMeasure uom) {
        return conversions.get(uom);
    }

    public Double addConversion(UnitOfMeasure uom, int factor) {
        return addConversion(uom, (double) factor);
    }

    public Double addConversion(UnitOfMeasure uom, Double factor) {
        Assert.notNull(uom, "Can't convert to the null UoM");
        Assert.notNull(factor, "UoM conversion factor's can't be null");
        uom.conversions.put(this, 1.0 / factor);
        return conversions.put(uom, factor);
    }

    public Double removeConversion(UnitOfMeasure uom) {
        return conversions.remove(uom);
    }

    public Map<UnitOfMeasure, Double> getConversions() {
        return Collections.unmodifiableMap(conversions);
    }

    @Override
    public String toString() {
        return getName();
    }

    public Quantity quantity(double quantity) {
        return new Quantity(quantity, this);
    }

    public UnitOfMeasure withAlias(String alias) {
        addAlias(alias);
        return this;
    }

}
