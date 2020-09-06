package com.brennaswitzer.cookbook.domain;

import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "_type")
@DiscriminatorValue("task")
public class Task extends BaseEntity implements Item {

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
    private String name;

    @Embedded
    private Quantity quantity;

    private String preparation;

    @NotNull
    private int position;

    @ManyToOne
    private Task parent;

    @ManyToOne(cascade = {CascadeType.MERGE})
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



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if (!isSubtask()) {
            this.position = position;
            return;
        }
        if (position > this.position) {
            boolean match = false;
            for (Task t : this.parent.subtasks) {
                if (t.position < this.position) continue;
                if (t.position > position) continue;
                if (t.position == position) {
                    match = true;
                    continue;
                }
                t.position -= 1;
            }
            if (match) position -= 1;
        } else {
            for (Task t : this.parent.subtasks) {
                if (t.position < position) continue;
                if (t.position >= this.position) continue;
                t.position += 1;
            }
        }
        this.position = position;
    }

    public boolean isSubtask() {
        return parent != null;
    }

    public boolean hasSubtasks() {
        return getSubtaskCount() != 0;
    }

    public Task getParent() {
        return parent;
    }

    public void setParent(Task parent) {
        if (this.parent != null && this.parent.subtasks != null) {
            if (! this.parent.subtasks.remove(this)) {
                throw new IllegalStateException("Task #" + getId() + " wasn't a subtask of it's parent #" + this.parent.getId() + "?!");
            }
            for (Task t : this.parent.subtasks) {
                if (t.position < this.position) continue;
                t.position -= 1;
            }
        }
        this.parent = parent;
        if (this.parent != null) {
            if (this.parent.subtasks == null) {
                this.parent.subtasks = new HashSet<>();
            }
            this.parent.subtasks.add(this);
            this.position = this.parent.subtasks.size() - 1;
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
        int position = after == null ? 0 : after.getPosition() + 1;
        if (task.parent != null && (after == null || task.parent != after.parent)) {
            task.parent.removeSubtask(task);
        }
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
        if (position < getSubtaskCount() - 1) task.setPosition(position);
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

    @Override
    public String getPreparation() {
        return preparation;
    }

    @Override
    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public void setPreparation(String preparation) {
        this.preparation = preparation;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }
}
