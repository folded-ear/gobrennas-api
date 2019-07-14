package com.brennaswitzer.cookbook.domain;

import org.springframework.util.Assert;

import javax.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "shopping_list")
public class ShoppingList extends BaseEntity {

    @Embeddable
    @Table(name = "shopping_list_item")
    public static class Item {

        private static final String MIXED_UNITS = "__mixed_units__";

        @ManyToOne
        private PantryItem ingredient;

        @ManyToOne
        private Task task;

        private String quantity;

        private String units;

        private Float amount;

        @Column(name = "completed_at")
        private Instant completedAt;

        public Item() {}

        public Item(String quantity, String units, PantryItem ingredient) {
            setQuantity(quantity);
            setUnits(units);
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
            this.quantity = quantity == null
                    ? null
                    : quantity.trim();
        }

        public boolean hasQuantity() {
            return quantity != null && !quantity.isEmpty();
        }

        public String getUnits() {
            return MIXED_UNITS.equals(units)
                    ? null
                    : units;
        }

        public void setUnits(String units) {
            this.units = units == null
                    ? null
                    : units.trim();
        }

        public boolean hasUnits() {
            return units != null && !MIXED_UNITS.equals(units) && !units.isEmpty();
        }

        public Float getAmount() {
            return amount;
        }

        public void setAmount(Float amount) {
            this.amount = amount;
        }

        public boolean hasAmount() {
            return amount != null;
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
            return buildToString().toString();
        }

        private StringBuilder buildToString() {
            StringBuilder sb = new StringBuilder().append(getIngredient().getName());
            if (!hasQuantity()) return sb;
            if (!hasUnits()) {
                if ("1".equals(getQuantity())) return sb;
                if ("one".equals(getQuantity())) return sb;
            }
            sb.append(" (").append(getQuantity());
            if (hasUnits()) {
                sb.append(' ').append(getUnits());
            }
            return sb.append(')');
        }

        public void add(String quantity, String units) {
            if (quantity != null) {
                quantity = quantity.trim();
                if (quantity.isEmpty()) quantity = null;
            }
            if (units != null) {
                units = units.trim();
                if (units.isEmpty()) units = null;
            }
            if (! hasQuantity()) setQuantity("1");
            if (quantity == null) quantity = "1";
            if (hasUnits() ? ! getUnits().equals(units) : units != null) {
                if (hasUnits()) setQuantity(getQuantity() + " " + getUnits());
                if (units != null) quantity = quantity + " " + units;
                setUnits(MIXED_UNITS);
            }
            setQuantity(getQuantity() + ", " + quantity);
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

    public void addPantryItem(String quantity, String units, PantryItem ingredient) {
        ensureItemMap();
        if (itemMap.containsKey(ingredient)) {
            itemMap.get(ingredient).add(quantity, units);
        } else {
            Item it = new Item(quantity, units, ingredient);
            itemMap.put(ingredient, it);
            items.add(it);
        }
    }

    public void addAllPantryItems(AggregateIngredient agg) {
        for (IngredientRef<PantryItem> ref : agg.assemblePantryItemRefs()) {
            addPantryItem(ref.getQuantity(), ref.getUnits(), ref.getIngredient());
        }
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
