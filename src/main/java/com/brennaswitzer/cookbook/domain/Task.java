package com.brennaswitzer.cookbook.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("WeakerAccess")
@Entity
public class Task extends BaseEntity {

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
    @ManyToOne
    private User owner;

    @NotNull
    private String name;

    @NotNull
    private BigDecimal quantity = BigDecimal.ONE;

    @NotNull
    private int position;

    @ManyToOne
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<Task> subtasks;

    public Task() {
    }

    public Task(String name) {
        setName(name);
    }

    public Task(User owner, String name) {
        setOwner(owner);
        setName(name);
    }

    Task(String name, int position) {
        setName(name);
        setPosition(position);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        if (quantity == null) quantity = BigDecimal.ONE;
        this.quantity = quantity;
    }

    public void setQuantity(int quantity) {
        setQuantity(BigDecimal.valueOf(quantity));
    }

    public boolean isQuantityInteresting() {
        return BigDecimal.ONE.compareTo(quantity) != 0;
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
            this.parent.subtasks.remove(this);
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

    public void addSubtask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("You can't add the null subtask");
        }
        task.setParent(this);
        task.setOwner(this.owner);
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
        if (isQuantityInteresting()) {
            sb.append(" (")
                    .append(quantity)
                    .append(')');
        }
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

    public Task mergeIn(Task dupe) {
        setQuantity(getQuantity().add(dupe.getQuantity()));
        return null;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
