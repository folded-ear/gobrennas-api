package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.repositories.PantryItemSearchRepository;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.collection.spi.PersistentSet;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@DiscriminatorValue("PantryItem")
@JsonTypeName("PantryItem")
public class PantryItem extends Ingredient {

    public static final Comparator<PantryItem> BY_STORE_ORDER = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        int c = a.getStoreOrder() - b.getStoreOrder();
        if (c != 0) {
            return c;
        }
        return a.getName().compareToIgnoreCase(b.getName());
    };

    // todo: make this user specific
    private int storeOrder = 0;

    @ElementCollection
    @Column(name = "synonym")
    @Setter(AccessLevel.PRIVATE)
    private Set<String> synonyms;

    /**
     * I cache this item's use count, but don't use me directly. Instead, use
     * {@link PantryItemSearchRepository#countTotalUses}.
     */
    private transient Long useCount;

    public PantryItem() {
    }

    public PantryItem(String name) {
        super(name);
    }

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        ensureNameIsNotSynonym();
    }

    @PreUpdate
    protected void onPreUpdate() {
        ensureNameIsNotSynonym();
    }

    /**
     * Don't let the name stick around as a synonym. More about tidiness than
     * correctness, but no sense bloating the table with pointless rows.
     */
    private void ensureNameIsNotSynonym() {
        if (synonyms == null) return;
        if (synonyms instanceof PersistentSet<?> pc && !pc.wasInitialized()) {
            // Don't make Hibernate load the synonyms just for this check.
            return;
        }
        var name = getName();
        if (name == null || name.isBlank()) return;
        if (!synonyms.remove(name)) {
            synonyms.removeIf(name::equalsIgnoreCase);
        }
    }

    /**
     * When changing the name, auto-register the old name as a synonym to help
     * mitigate changes in parsing of raw ingredient refs. Every pantry item
     * name has had at least one raw reference to its current name. Most still
     * will, and many will have others, likely _without_ explicit delimiters.
     */
    @Override
    public void setName(String name) {
        var oldName = getName();
        if (oldName != null && !oldName.equalsIgnoreCase(name) && !oldName.isBlank()) {
            addSynonym(oldName);
        }
        super.setName(name);
    }

    @Override
    public boolean answersToName(String name) {
        return super.answersToName(name)
                || hasSynonym(name);
    }

    public boolean hasSynonym(String synonym) {
        if (synonyms == null) return false;
        if (synonym == null || synonym.isBlank()) return false;
        if (synonyms.contains(synonym)) return true;
        for (var syn : synonyms)
            if (synonym.equalsIgnoreCase(syn)) return true;
        return false;
    }

    public boolean addSynonym(String synonym) {
        Assert.notNull(synonym, "Null isn't a valid synonym");
        if (hasSynonym(synonym)) return false;
        if (synonyms == null) synonyms = new HashSet<>();
        return synonyms.add(synonym);
    }

    public void addSynonyms(String... synonyms) {
        for (String a : synonyms) addSynonym(a);
    }

    public PantryItem withSynonym(String synonym) {
        addSynonym(synonym);
        return this;
    }

    public PantryItem withSynonyms(String... synonyms) {
        addSynonyms(synonyms);
        return this;
    }

    public boolean removeSynonym(String synonym) {
        if (synonyms == null) return false;
        return synonyms.remove(synonym)
                || synonyms.removeIf(synonym::equalsIgnoreCase);
    }

    public Set<String> getSynonyms() {
        if (synonyms == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(synonyms);
    }

    public void clearSynonyms() {
        if (synonyms == null) {
            return;
        }
        synonyms.clear();
    }

    @Override
    public String toString() {
        return getName();
    }

}
