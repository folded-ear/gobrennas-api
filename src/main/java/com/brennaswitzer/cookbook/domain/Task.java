package com.brennaswitzer.cookbook.domain;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("WeakerAccess")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "_type")
@DiscriminatorValue("task")
public class Task extends BaseEntity implements MutableItem<Ingredient> {

    public static final Comparator<Task> BY_NAME = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.name.compareTo(b.name);
    };

    public static final Comparator<Task> BY_NAME_IGNORE_CASE = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.name.compareToIgnoreCase(b.name);
    };

    public static final Comparator<Task> BY_ORDER = (a, b) -> {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        return a.position - b.position;
    };

    @NotNull
    @Getter
    @Setter
    private String name;

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
    private int position;

    @ManyToOne
    @Getter
    private Task parent;

    @ManyToOne(cascade = {CascadeType.MERGE})
    @Getter
    @Setter
    private Ingredient ingredient;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @BatchSize(size = 100)
    private Set<Task> subtasks;

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

    public void setPosition(int position) {
        if (!isSubtask()) {
            this.position = position;
            return;
        }
        // ensure there's a hole
        for (Task t : this.parent.subtasks) {
            if (t.position >= position) t.position += 1;
        }
        this.position = position;
        this.parent.resetSubtaskPositions();
    }

    private void resetSubtaskPositions() {
        AtomicInteger seq = new AtomicInteger();
        for (Task t : getOrderedSubtasksView()) {
            t.position = seq.getAndIncrement();
        }
    }

    public boolean isSubtask() {
        return parent != null;
    }

    public boolean hasSubtasks() {
        return getSubtaskCount() != 0;
    }

    public void setParent(Task parent) {
        if (this.parent != null && this.parent.subtasks != null) {
            if (! this.parent.subtasks.remove(this)) {
                throw new IllegalStateException("Task #" + getId() + " wasn't a subtask of it's parent #" + this.parent.getId() + "?!");
            }
            this.parent.resetSubtaskPositions();
        }
        this.parent = parent;
        if (this.parent != null) {
            if (this.parent.subtasks == null) {
                this.parent.subtasks = new HashSet<>();
            }
            this.parent.subtasks.add(this);
            this.position = this.parent.subtasks.size() - 1;
            this.parent.resetSubtaskPositions();
        }
    }

    public boolean hasParent() {
        return parent != null;
    }

    public TaskList getTaskList() {
        return parent.getTaskList();
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

    public void addSubtaskAfter(Task task, Task after) {
        if (task == null) {
            throw new IllegalArgumentException("You can't add the null subtask");
        }
        if (after != null && after.parent != this) {
            throw new IllegalArgumentException("The 'after' task isn't a child of this; that makes no sense.");
        }
        if (task.parent != null) {
            task.parent.removeSubtask(task);
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
        task.setPosition(position);
    }

    public void removeSubtask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("You can't remove the null subtask");
        }
        task.setParent(null);
    }

    public Collection<Task> getSubtaskView() {
        if (subtasks == null) {
            //noinspection unchecked
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(subtasks);
    }

    public List<Task> getOrderedSubtasksView() {
        return getSubtaskView(BY_ORDER);
    }

    public List<Task> getSubtaskView(Comparator<Task> comparator) {
        if (subtasks == null) {
            //noinspection unchecked
            return Collections.EMPTY_LIST;
        }
        List<Task> list = new ArrayList<>(subtasks);
        list.sort(comparator);
        return list;
    }

    public int getSubtaskCount() {
        return subtasks == null ? 0 : subtasks.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (isSubtask()) {
            sb.append(" [")
                    .append(parent.name) // NOT .toString()!
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
        return of(after.parent, after);
    }

    @Override
    public String getRaw() {
        return name;
    }

    @Override
    public Quantity getQuantity() {
        if (quantity == null) return Quantity.ONE;
        return quantity;
    }

    public boolean hasIngredient() {
        return ingredient != null;
    }

}
