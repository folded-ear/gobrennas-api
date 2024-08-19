package com.brennaswitzer.cookbook.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;

@SuppressWarnings("WeakerAccess")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@DiscriminatorValue("item")
public class PlanItem extends BaseEntity implements Named, MutableItem {

    public static final Comparator<PlanItem> BY_ID = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getId().compareTo(b.getId());
    };

    public static final Comparator<PlanItem> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareTo(b.getName());
    };

    public static final Comparator<PlanItem> BY_NAME_IGNORE_CASE = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareToIgnoreCase(b.getName());
    };

    public static final Comparator<PlanItem> BY_ORDER = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        int c = a.getPosition() - b.getPosition();
        if (c != 0) return c;
        return a.getName().compareToIgnoreCase(b.getName());
    };

    @NotNull
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String notes;

    @Column(name = "status_id")
    @Getter
    @Setter
    private PlanItemStatus status = PlanItemStatus.NEEDED;

    @Embedded
    @Setter
    private Quantity quantity;

    @Getter
    @Setter
    private String preparation;

    @NotNull
    @Getter
    @Setter
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    private PlanItem parent;

    @OneToMany(
            mappedBy = "parent",
            cascade = ALL)
    @BatchSize(size = 50)
    private Set<PlanItem> children;

    @ManyToOne(fetch = FetchType.LAZY)
    private Plan trashBin;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    private PlanItem aggregate;

    @OneToMany(
            mappedBy = "aggregate",
            cascade = { PERSIST, MERGE, REFRESH, DETACH })
    @BatchSize(size = 50)
    private Set<PlanItem> components;

    @ManyToOne(
            cascade = MERGE,
            fetch = FetchType.LAZY)
    @Getter
    @Setter
    private Ingredient ingredient;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private PlanBucket bucket;

    @Getter
    @Setter
    @Column(name = "mod_count")
    private int modCount;

    public PlanItem() {
    }

    public PlanItem(String name) {
        setName(name);
    }

    PlanItem(String name, int position) {
        setName(name);
        setPosition(position);
    }

    public PlanItem(String name, Quantity quantity, Ingredient ingredient, String preparation) {
        setName(name);
        setQuantity(quantity);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    public PlanItem(String name, Ingredient ingredient) {
        this(name, null, ingredient, null);
    }

    @Override
    protected void onPrePersist() {
        super.onPrePersist();
        setModCount(1);
    }

    @PreUpdate
    protected void onPreUpdate() {
        setModCount(getModCount() + 1);
    }

    public void setChildPosition(PlanItem child, int position) {
        AtomicInteger seq = new AtomicInteger();
        boolean pending = true;
        for (PlanItem t : getOrderedChildView()) {
            if (t.equals(child)) continue;
            int min = seq.getAndIncrement();
            if (pending && min >= position) {
                pending = false;
                child.setPosition(position);
                min = seq.getAndIncrement();
            }
            int curr = t.getPosition();
            if (curr < min) {
                t.setPosition(min);
            } else if (curr > min) {
                seq.set(curr + 1);
            }
        }
        if (pending) {
            child.setPosition(seq.get());
        }
    }

    public boolean isChild() {
        return getParent() != null;
    }

    public boolean isComponent() {
        return getAggregate() != null;
    }

    public boolean hasChildren() {
        return getChildCount() != 0;
    }

    public boolean hasComponents() {
        return getComponentCount() != 0;
    }

    public boolean isDescendant(PlanItem t) {
        for (; t != null; t = t.getParent()) {
            if (t == this) return true;
        }
        return false;
    }

    public boolean isDescendantComponent(PlanItem t) {
        for (; t != null; t = t.getAggregate()) {
            if (t == this) return true;
        }
        return false;
    }

    public void setParent(PlanItem parent) {
        PlanItem currentParent = (PlanItem) Hibernate.unproxy(getParent());
        // see if it's a no-op
        if (Objects.equals(parent, currentParent)) {
            return;
        }
        if (isDescendant(parent)) {
            throw new IllegalArgumentException("You can't make an item a descendant of one of its own descendants");
        }
        // tear down the old one
        if (currentParent != null && currentParent.children != null) {
            if (!currentParent.children.remove(this)) {
                throw new IllegalStateException("Item #" + getId() + " wasn't a child of its parent #" + currentParent.getId() + "?!");
            }
            currentParent.markDirty();
        }
        // wire up the new one
        if (parent != null) {
            if (parent.children == null) {
                parent.children = new HashSet<>();
            }
            if (parent.children.add(this)) {
                setPosition(1 + parent.children
                        .stream()
                        .map(PlanItem::getPosition)
                        .reduce(0, Integer::max));
            }
            parent.markDirty();
        }
        this.parent = parent;
    }

    public boolean isRecognitionDisallowed() {
        return getName().startsWith("!");
    }

    public void moveToTrash() {
        this.trashBin = getPlan();
        this.trashBin.getTrashBinItems().add(this);
        if (!this.status.isForDelete()) {
            this.status = PlanItemStatus.DELETED;
        }
        getParent().markDirty();
    }

    public boolean isInTrashBin() {
        return this.trashBin != null;
    }

    public void restoreFromTrash() {
        if (!isInTrashBin()) {
            throw new IllegalArgumentException("This item is not in the trash");
        }
        if (getParent().isInTrashBin()) {
            // can't put it back where it was, so top-level it is!
            setParent(this.trashBin);
        }
        this.trashBin.getTrashBinItems().remove(this);
        this.trashBin = null;
        getParent().markDirty();
        this.status = PlanItemStatus.NEEDED;
    }

    public void setAggregate(PlanItem agg) {
        if (agg == null ? getAggregate() == null : agg.equals(getAggregate())) {
            return;
        }
        if (isDescendantComponent(agg)) {
            throw new IllegalArgumentException("You can't make an item a component of one of its own components");
        }
        if (getAggregate() != null && getAggregate().components != null) {
            if (!getAggregate().components.remove(this)) {
                throw new IllegalStateException("Item #" + getId() + " wasn't a component of its aggregate #" + getAggregate().getId() + "?!");
            }
        }
        if (agg != null) {
            if (agg.components == null) {
                agg.components = new HashSet<>();
            }
        }
        this.aggregate = agg;
    }

    public boolean isAggregated() {
        return this.aggregate != null;
    }

    public boolean hasParent() {
        return getParent() != null;
    }

    public Plan getPlan() {
        return getParent().getPlan();
    }

    /**
     * Add a new PlanItem to the end of this item's children.
     *
     * @param item the item to add.
     */
    public void addChild(PlanItem item) {
        if (item == null) {
            throw new IllegalArgumentException("You can't add a null child item");
        }
        item.setParent(this);
    }

    /**
     * Add a new PlanItem as both a child and component of this item.
     *
     * @param t the item to add as a component
     */
    public void addAggregateComponent(PlanItem t) {
        addChild(t);
        t.setAggregate(this);
    }

    public void addChildAfter(PlanItem child, PlanItem after) {
        if (child == null) {
            throw new IllegalArgumentException("You can't add a null child");
        }
        if (after != null && !this.equals(after.getParent())) {
            throw new IllegalArgumentException("The 'after' item isn't a child of this; that makes no sense.");
        }
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        int position = after == null ? 0 : after.getPosition() + 1;
        insertChild(position, child);
    }

    public void insertChild(int position, PlanItem child) {
        if (position < 0) {
            throw new IllegalArgumentException("You can't insert an child at a negative position");
        }
        if (child == null) {
            throw new IllegalArgumentException("You can't insert a null child");
        }
        addChild(child);
        setChildPosition(child, position);
    }

    public void removeChild(PlanItem child) {
        if (child == null) {
            throw new IllegalArgumentException("You can't remove a null item");
        }
        child.setParent(null);
    }

    public Collection<PlanItem> getChildView() {
        // I have no idea why HashSet::new doesn't work here, while
        // ArrayList::new is just fine
        return Collections.unmodifiableSet(getChildView(() -> new HashSet<>()));
    }

    private <T extends Collection<PlanItem>> T getChildView(Supplier<T> collectionSupplier) {
        if (children == null) {
            return collectionSupplier.get();
        }
        return children.stream()
                .filter(Predicate.not(PlanItem::isInTrashBin))
                .collect(Collectors.toCollection(collectionSupplier));
    }

    public List<PlanItem> getOrderedChildView() {
        return getChildView(BY_ORDER);
    }

    public List<PlanItem> getChildView(Comparator<PlanItem> comparator) {
        val items = getChildView(ArrayList::new);
        items.sort(comparator);
        return items;
    }

    public List<PlanItem> getOrderedComponentsView() {
        return getComponentView(BY_ID);
    }

    public List<PlanItem> getComponentView(Comparator<PlanItem> comparator) {
        if (components == null) {
            return Collections.emptyList();
        }
        List<PlanItem> items = new ArrayList<>(components);
        items.sort(comparator);
        return items;
    }

    public int getChildCount() {
        return children == null ? 0 : children.size();
    }

    public int getComponentCount() {
        return components == null ? 0 : components.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (isChild()) {
            sb.append(" [")
                    .append(getParent().getName()) // NOT .toString()!
                    .append(']');
        }
        return sb.toString();
    }

    public PlanItem of(PlanItem parent) {
        parent.addChild(this);
        return this;
    }

    public PlanItem of(PlanItem parent, PlanItem after) {
        parent.addChildAfter(this, after);
        return this;
    }

    public PlanItem after(PlanItem after) {
        return of(after.getParent(), after);
    }

    @Override
    public String getRaw() {
        return getName();
    }

    @Override
    public Quantity getQuantity() {
        if (quantity == null) return Quantity.ONE;
        return quantity;
    }

    public boolean hasIngredient() {
        return ingredient != null;
    }

    public boolean hasBucket() {
        return bucket != null;
    }

    public boolean hasNotes() {
        return notes != null && !notes.isEmpty();
    }

}
