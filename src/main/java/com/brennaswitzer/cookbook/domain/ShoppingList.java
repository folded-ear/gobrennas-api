package com.brennaswitzer.cookbook.domain;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "shopping_list")
public class ShoppingList extends BaseEntity {

    @Embeddable
    @Table(name = "shopping_list_item")
    public static class Item {

        @ManyToOne
        private PantryItem ingredient;

        @ManyToOne
        private Task task;

        private String quantity;

        @Column(name = "completed_at")
        private Instant completedAt;

        public Item() {}

        public Item(String quantity, PantryItem ingredient) {
            setQuantity(quantity);
            setIngredient(ingredient);
        }

        public PantryItem getIngredient() {
            return ingredient;
        }

        public void setIngredient(PantryItem ingredient) {
            this.ingredient = ingredient;
        }

        public Task getTask() {
            if (task == null) {
                task = new Task(toString());
            }
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }

        public String getQuantity() {
            return quantity;
        }

        public void setQuantity(String quantity) {
            this.quantity = quantity;
        }

        public Instant getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
        }

        public void markComplete() {
            if (isComplete()) {
                throw new IllegalStateException("This item is already complete");
            }
            setCompletedAt(Instant.now());
        }

        public boolean isComplete() {
            return completedAt != null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder().append(getIngredient().getName());
            if (getQuantity() != null && !getQuantity().isEmpty()) {
                sb.append(" (").append(getQuantity()).append(')');
            }
            return sb.toString();
        }

        private String add(String a, String b, String def) {
            if (a == null || a.isEmpty()) a = def;
            if (b == null || b.isEmpty()) b = def;
            if (a == null) return b;
            if (b == null) return a;
            return a + ", " + b;
        }

        public void add(String quantity) {
            setQuantity(add(getQuantity(), quantity, "1"));
        }
    }

    // this should be a Set, but it makes assertions harder, because iteration order is undefined
    @ElementCollection
    private List<Item> items = new LinkedList<>();

    transient private Map<PantryItem, Item> itemMap;
    private void ensureItemMap() {
        if (itemMap == null) {
            itemMap = new HashMap<>();
            for (Item it : items) itemMap.put(it.ingredient, it);
        }
    }

    public ShoppingList() {}

    public List<Item> getListItems() {
        return items;
    }

    public void addPantryItem(String quantity, PantryItem ingredient) {
        ensureItemMap();
        if (itemMap.containsKey(ingredient)) {
            itemMap.get(ingredient).add(quantity);
        } else {
            Item it = new Item(quantity, ingredient);
            itemMap.put(ingredient, it);
            items.add(it);
        }
    }

    public void addAllPantryItems(AggregateIngredient agg) {
        for (IngredientRef<PantryItem> ref : agg.getPurchasableSchmankies()) {
            addPantryItem(ref.getQuantity(), ref.getIngredient());
        }
    }

    public void createTasks(Task taskList) {
        // todo: do magical ordering things!
        for (Item it : items) {
            taskList.addSubtask(it.getTask());
        }
    }

    public void createTasks(String heading, Task taskList) {
        taskList.addSubtask(new Task(heading + ":"));
        createTasks(taskList);
    }

    public void taskCompleted(Long id) {
        Assert.notNull(id, "Completing the null task makes no sense?!");
        items
                .stream()
                .filter(it -> it.task != null && id.equals(it.task.getId()))
                .filter(it -> ! it.isComplete())
                .forEach(Item::markComplete);
    }

}
