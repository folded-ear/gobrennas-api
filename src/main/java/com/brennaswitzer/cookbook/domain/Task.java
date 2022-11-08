package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.hibernate.Hibernate;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.*;

@SuppressWarnings("WeakerAccess")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "_type")
@DiscriminatorValue("item")
public class Task extends BaseEntity implements MutableItem {

    public static final Comparator<Task> BY_ID = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getId().compareTo(b.getId());
    };

    public static final Comparator<Task> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareTo(b.getName());
    };

    public static final Comparator<Task> BY_NAME_IGNORE_CASE = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.getName().compareToIgnoreCase(b.getName());
    };

    public static final Comparator<Task> BY_ORDER = (a, b) -> {
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
    private TaskStatus status = TaskStatus.NEEDED;

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

    @ManyToOne
    @Getter
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = ALL)
    @BatchSize(size = 100)
    private Set<Task> subtasks;

    @ManyToOne
    private TaskList trashBin;

    @ManyToOne
    @Getter
    private Task aggregate;

    @OneToMany(mappedBy = "aggregate", cascade = {PERSIST, MERGE, REFRESH, DETACH})
    @BatchSize(size = 100)
    private Set<Task> components;

    @ManyToOne(cascade = MERGE)
    @Getter
    @Setter
    private Ingredient ingredient;

    @Getter
    @Setter
    @ManyToOne
    private PlanBucket bucket;

    public Task() {
    }

    public Task(String name) {
        setName(name);
    }

    Task(String name, int position) {
        setName(name);
        setPosition(position);
    }

    public Task(String name, Quantity quantity, Ingredient ingredient, String preparation) {
        setName(name);
        setQuantity(quantity);
        setIngredient(ingredient);
        setPreparation(preparation);
    }

    public Task(String name, Ingredient ingredient) {
        this(name, null, ingredient, null);
    }

    public void setChildPosition(Task child, int position) {
        AtomicInteger seq = new AtomicInteger();
        boolean pending = true;
        for (Task t : getOrderedSubtasksView()) {
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

    public boolean isSubtask() {
        return getParent() != null;
    }

    public boolean isComponent() {
        return getAggregate() != null;
    }

    public boolean hasSubtasks() {
        return getSubtaskCount() != 0;
    }

    public boolean hasComponents() {
        return getComponentCount() != 0;
    }

    public boolean isDescendant(Task t) {
        for (; t != null; t = t.getParent()) {
            if (t == this) return true;
        }
        return false;
    }

    public boolean isDescendantComponent(Task t) {
        for (; t != null; t = t.getAggregate()) {
            if (t == this) return true;
        }
        return false;
    }

    public void setParent(Task parent) {
        Task currentParent = (Task) Hibernate.unproxy(getParent());
        // see if it's a no-op
        if (Objects.equals(parent, currentParent)) {
            return;
        }
        if (isDescendant(parent)) {
            throw new IllegalArgumentException("You can't make a task a descendant of one of its own descendants");
        }
        // tear down the old one
        if (currentParent != null && currentParent.subtasks != null) {
            if (!currentParent.subtasks.remove(this)) {
                throw new IllegalStateException("Task #" + getId() + " wasn't a subtask of its parent #" + currentParent.getId() + "?!");
            }
            currentParent.markDirty();
        }
        // wire up the new one
        if (parent != null) {
            if (parent.subtasks == null) {
                parent.subtasks = new HashSet<>();
            }
            if (parent.subtasks.add(this)) {
                setPosition(1 + parent.subtasks
                        .stream()
                        .map(Task::getPosition)
                        .reduce(0, Integer::max));
            }
            parent.markDirty();
        }
        this.parent = parent;
    }

    public void moveToTrash() {
        this.trashBin = getTaskList();
        this.trashBin.getTrashBinTasks().add(this);
        this.status = TaskStatus.DELETED;
        getParent().markDirty();
    }

    public boolean isInTrashBin() {
        return this.trashBin != null;
    }

    public void restoreFromTrash() {
        if (!isInTrashBin()) {
            throw new IllegalArgumentException("This task is not in the trash");
        }
        if (getParent().isInTrashBin()) {
            // can't put it back where it was, so top-level it is!
            setParent(this.trashBin);
        }
        this.trashBin.getTrashBinTasks().remove(this);
        this.trashBin = null;
        getParent().markDirty();
        this.status = TaskStatus.NEEDED;
    }

    public void setAggregate(Task agg) {
        if (agg == null ? getAggregate() == null : agg.equals(getAggregate())) {
            return;
        }
        if (isDescendantComponent(agg)) {
            throw new IllegalArgumentException("You can't make a task a component of one of its own components");
        }
        if (getAggregate() != null && getAggregate().components != null) {
            if (!getAggregate().components.remove(this)) {
                throw new IllegalStateException("Task #" + getId() + " wasn't a component of its aggregate #" + getAggregate().getId() + "?!");
            }
        }
        if (agg != null) {
            if (agg.components == null) {
                agg.components = new HashSet<>();
            }
        }
        this.aggregate = agg;
    }

    public boolean hasParent() {
        return getParent() != null;
    }

    public TaskList getTaskList() {
        return getParent().getTaskList();
    }

    /**
     * Add a new Task to the end of this list.
     * @param task the task to add.
     */
    public void addSubtask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("You can't add the null subtask");
        }
        task.setParent(this);
    }

    /**
     * Add a new Task as both a child and component of this task.
     * @param t the task to add as a component
     */
    public void addAggregateComponent(Task t) {
        addSubtask(t);
        t.setAggregate(this);
    }

    public void addSubtaskAfter(Task task, Task after) {
        if (task == null) {
            throw new IllegalArgumentException("You can't add the null subtask");
        }
        if (after != null && !this.equals(after.getParent())) {
            throw new IllegalArgumentException("The 'after' task isn't a child of this; that makes no sense.");
        }
        if (task.getParent() != null) {
            task.getParent().removeSubtask(task);
        }
        int position = after == null ? 0 : after.getPosition() + 1;
        insertSubtask(position, task);
    }

    public void insertSubtask(int position, Task task) {
        if (position < 0) {
            throw new IllegalArgumentException("You can't insert a task at a negative position");
        }
        if (task == null) {
            throw new IllegalArgumentException("You can't add the null subtask");
        }
        addSubtask(task);
        setChildPosition(task, position);
    }

    public void removeSubtask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("You can't remove the null subtask");
        }
        task.setParent(null);
    }

    public Collection<Task> getSubtaskView() {
        // I have no idea why HashSet::new doesn't work here, while
        // ArrayList::new is just fine
        return Collections.unmodifiableSet(getSubtaskView(() ->
                new HashSet<>()));
    }

    private <T extends Collection<Task>> T getSubtaskView(Supplier<T> collectionSupplier) {
        if (subtasks == null) {
            return collectionSupplier.get();
        }
        return subtasks.stream()
                .filter(t -> !t.isInTrashBin()) // Predicate.not is Java 11 :(
                .collect(Collectors.toCollection(collectionSupplier));
    }

    public List<Task> getOrderedSubtasksView() {
        return getSubtaskView(BY_ORDER);
    }

    public List<Task> getSubtaskView(Comparator<Task> comparator) {
        val list = getSubtaskView(ArrayList::new);
        list.sort(comparator);
        return list;
    }

    public List<Task> getOrderedComponentsView() {
        return getComponentView(BY_ID);
    }

    public List<Task> getComponentView(Comparator<Task> comparator) {
        if (components == null) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        List<Task> list = new ArrayList<>(components);
        list.sort(comparator);
        return list;
    }

    public int getSubtaskCount() {
        return subtasks == null ? 0 : subtasks.size();
    }

    public int getComponentCount() {
        return components == null ? 0 : components.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        if (isSubtask()) {
            sb.append(" [")
                    .append(getParent().getName()) // NOT .toString()!
                    .append(']');
        }
        return sb.toString();
    }

    public Task of(Task parent) {
        parent.addSubtask(this);
        return this;
    }

    public Task of(Task parent, Task after) {
        parent.addSubtaskAfter(this, after);
        return this;
    }

    public Task after(Task after) {
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
