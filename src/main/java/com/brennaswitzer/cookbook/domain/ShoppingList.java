package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.NumberUtils;
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

        public Item(IngredientRef<PantryItem> ref) {
            if (ref.hasAmount()) {
                setAmount(ref.getAmount());
            } else {
                setQuantity(ref.getQuantity());
            }
            setUnits(ref.getUnits());
            setIngredient(ref.getIngredient());
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
            if (!hasAmount() && !hasQuantity()) return sb;
            String amount = hasAmount()
                    ? NumberUtils.formatFloat(getAmount())
                    : getQuantity();
            if (!hasUnits()) {
                if ("1".equals(amount)) return sb;
                if ("one".equals(amount)) return sb;
            }
            sb.append(" (").append(amount);
            if (hasUnits()) {
                sb.append(' ').append(getUnits());
            }
            return sb.append(')');
        }

        public void add(IngredientRef<PantryItem> ref) {
            assert ingredient.equals(ref.getIngredient());
            boolean unitsMatch = hasUnits()
                    ? ref.hasUnits() && getUnits().equals(ref.getUnits())
                    : !ref.hasUnits();
            if (hasAmount()) {
                if (ref.hasAmount() && unitsMatch) {
                    // woo! happy path!
                    setAmount(getAmount() + ref.getAmount());
                    return;
                }
                setQuantity(NumberUtils.formatFloat(getAmount()));
                setAmount(null);
            }
            // we're doing quantity. :|
            String refQuantity = ref.hasAmount()
                    ? NumberUtils.formatFloat(ref.getAmount())
                    : ref.getQuantity();
            if (unitsMatch) {
                setQuantity(getQuantity() + ", " + refQuantity);
                return;
            }
            // and with mixed units...
            StringBuilder sb = new StringBuilder(getQuantity());
            if (hasUnits()) sb.append(' ').append(getUnits());
            sb.append(", ").append(refQuantity);
            if (ref.hasUnits()) sb.append(' ').append(ref.getUnits());
            setQuantity(sb.toString());
            setUnits(MIXED_UNITS);
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
