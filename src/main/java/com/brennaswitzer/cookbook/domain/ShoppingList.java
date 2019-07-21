package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.NumberUtils;
import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "shopping_list")
public class ShoppingList extends BaseEntity {

    private static final Comparator<String> STRING_BY_CASE_INSENSITIVE_NULL_FIRST = (a, b) -> {
        if (a == null) return b == null ? 0 : -1;
        if (b == null) return 1;
        return a.compareToIgnoreCase(b);
    };

    @Embeddable
    @Table(name = "shopping_list_item")
    public static class Item {

        @ManyToOne
        private PantryItem ingredient;

        @ManyToOne
        private Task task;

        private String quantity;

        transient private Map<String, Double> byUnit;

        @Column(name = "completed_at")
        private Instant completedAt;

        public Item() {}

        public Item(IngredientRef<PantryItem> ref) {
            setIngredient(ref.getIngredient());
            add(ref);
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
            if (!"1".equals(getQuantity())) {
                sb.append(" (").append(getQuantity()).append(')');
            }
            return sb.toString();
        }

        private void ensureUnitMap(IngredientRef ref) {
            if (byUnit == null) {
                byUnit = new TreeMap<>(STRING_BY_CASE_INSENSITIVE_NULL_FIRST);
            }
            byUnit.put(ref.getUnits(), (byUnit.containsKey(ref.getUnits())
                    ? byUnit.get(ref.getUnits())
                    : 0) + ref.getQuantity());
        }

        public void add(IngredientRef<PantryItem> ref) {
            if (!ingredient.equals(ref.getIngredient())) {
                throw new IllegalArgumentException("Can't aggregate unlike ingredients");
            }
            ensureUnitMap(ref);
            StringBuilder sb = new StringBuilder();
            for (String units : byUnit.keySet()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(NumberUtils.formatNumber(byUnit.get(units)));
                if (units != null) {
                    sb.append(' ').append(units);
                }
            }
            setQuantity(sb.toString());
        }

    }

    // this should be a Set, but it makes assertions harder, because iteration order is undefined
    @ElementCollection
    @OrderBy("completedAt")
    private List<Item> items = new LinkedList<>();

    transient private Map<PantryItem, Item> itemMap;
    private void ensureItemMap() {
        if (itemMap == null) {
            itemMap = new HashMap<>();
            for (Item it : items) itemMap.put(it.ingredient, it);
        }
    }

    public ShoppingList() {}

    public List<Item> getItems() {
        return items;
    }

    public void resetItemOrder(Comparator<Item> comparator) {
        if (items == null) return;
        items.sort(comparator);
    }

    public void addPantryItem(IngredientRef<PantryItem> ref) {
        ensureItemMap();
        PantryItem ingredient = ref.getIngredient();
        if (itemMap.containsKey(ingredient)) {
            itemMap.get(ingredient).add(ref);
        } else {
            Item it = new Item(ref);
            itemMap.put(ingredient, it);
            items.add(it);
        }
    }

    public void addAllPantryItems(AggregateIngredient agg) {
        agg.assemblePantryItemRefs().forEach(this::addPantryItem);
    }

    public void createTasks(Task taskList) {
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
        items.stream()
                .filter(it -> it.task != null && id.equals(it.task.getId()))
                .filter(it -> !it.isComplete())
                .forEach(Item::markComplete);
    }

}
