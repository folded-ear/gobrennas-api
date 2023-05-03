package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.TaskTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlanItemTest {

    private void assertBefore(PlanItem first, PlanItem second) {
        assertTrue(
                first.getPosition() < second.getPosition(),
                first.getName() + " (" + first.getPosition() + ") is before " + second.getName() + " (" + second.getPosition() + ")"
        );
    }

    @Test
    public void addSubtask_basics() {
        PlanItem groceries = new PlanItem("Groceries");
        assertFalse(groceries.isSubtask());
        assertFalse(groceries.hasSubtasks());
        assertEquals(0, groceries.getSubtaskCount());

        PlanItem oj = new PlanItem("OJ");
        groceries.addSubtask(oj);

        assertFalse(groceries.isSubtask());
        assertTrue(groceries.hasSubtasks());
        assertEquals(1, groceries.getSubtaskCount());

        assertTrue(oj.isSubtask());
        assertSame(groceries, oj.getParent());
        assertSame(oj, groceries.getSubtaskView().iterator().next());
    }

    @Test
    public void addSubtask_ordering() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addSubtask(apples);
        groceries.addSubtask(oj);
        groceries.addSubtask(bagels);
        groceries.addSubtask(iceCream);

        assertBefore(apples, oj);
        assertBefore(oj, bagels);
        assertBefore(bagels, iceCream);
    }

    @Test
    public void addSubtaskAfter() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addSubtask(oj);
        groceries.addSubtask(bagels);
        groceries.addSubtaskAfter(apples, null);
        groceries.addSubtaskAfter(iceCream, apples);

        assertBefore(apples, iceCream);
        assertBefore(iceCream, oj);
        assertBefore(oj, bagels);

        // oops, Ice Cream is after OJ

        groceries.addSubtaskAfter(iceCream, oj);

        assertBefore(apples, oj);
        assertBefore(oj, iceCream);
        assertBefore(iceCream, bagels);
    }

    @Test
    public void insertSubtask() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addSubtask(apples);
        groceries.addSubtask(oj);
        groceries.addSubtask(bagels);
        groceries.insertSubtask(bagels.getPosition(), iceCream);

        assertBefore(apples, oj);
        assertBefore(oj, iceCream);
        assertBefore(iceCream, bagels);
    }

    @Test
    public void setChildPosition() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem chicken = new PlanItem("Chicken");
        groceries.addSubtask(apples);
        groceries.addSubtask(bagels);
        groceries.addSubtask(chicken);
        assertBefore(apples, bagels);
        assertBefore(bagels, chicken);

        groceries.setChildPosition(bagels, 0);

        assertBefore(bagels, apples);
        assertBefore(apples, chicken);

        groceries.setChildPosition(bagels, 2);

        assertBefore(apples, bagels);
        assertBefore(bagels, chicken);
    }

    @Test
    public void toString_() {
        PlanItem groceries = new PlanItem("Groceries");
        assertEquals("Groceries", groceries.toString());

        PlanItem oj = new PlanItem("OJ");
        groceries.addSubtask(oj);

        assertEquals("Groceries", groceries.toString());
        assertEquals("OJ [Groceries]", oj.toString());

        PlanItem orange = new PlanItem("Orange");
        oj.addSubtask(orange);

        assertEquals("OJ [Groceries]", oj.toString());
        assertEquals("Orange [OJ]", orange.toString());
    }

    @Test
    public void BY_NAME() {
        PlanItem a = new PlanItem("a");
        //noinspection EqualsWithItself
        assertEquals(0, PlanItem.BY_NAME.compare(a, a));
        assertTrue(PlanItem.BY_NAME.compare(a, null) < 0);
        assertTrue(PlanItem.BY_NAME.compare(null, a) > 0);
        PlanItem b = new PlanItem("b");
        assertTrue(PlanItem.BY_NAME.compare(a, b) < 0);
        assertTrue(PlanItem.BY_NAME.compare(b, a) > 0);

        // UPPERCASE < lowercase
        PlanItem B = new PlanItem("B");
        assertTrue(PlanItem.BY_NAME.compare(a, B) > 0);
        assertTrue(PlanItem.BY_NAME.compare(B, a) < 0);
    }

    @Test
    public void BY_NAME_IGNORE_CASE() {
        PlanItem a = new PlanItem("a");
        //noinspection EqualsWithItself
        assertEquals(0, PlanItem.BY_NAME_IGNORE_CASE.compare(a, a));
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(a, null) < 0);
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(null, a) > 0);
        PlanItem b = new PlanItem("b");
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(a, b) < 0);
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(b, a) > 0);

        PlanItem B = new PlanItem("B");
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(a, B) < 0);
        assertTrue(PlanItem.BY_NAME_IGNORE_CASE.compare(B, a) > 0);
    }

    @Test
    public void BY_ORDER() {
        PlanItem a = new PlanItem("", 1);
        //noinspection EqualsWithItself
        assertEquals(0, PlanItem.BY_ORDER.compare(a, a));
        assertTrue(PlanItem.BY_ORDER.compare(a, null) < 0);
        assertTrue(PlanItem.BY_ORDER.compare(null, a) > 0);
        PlanItem b = new PlanItem("", 2);
        assertTrue(PlanItem.BY_ORDER.compare(a, b) < 0);
        assertTrue(PlanItem.BY_ORDER.compare(b, a) > 0);
    }

    @Test
    public void muppetLikeListsForShopping() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem tacos = new PlanItem("Tacos").of(groceries);
        PlanItem salad = new PlanItem("Salad").of(groceries);
        PlanItem lunch = new PlanItem("Lunch").of(groceries);

        System.out.println(renderTree("Meals", groceries));

        PlanItem meat = new PlanItem("meat").of(tacos);
        PlanItem tortillas = new PlanItem("tortillas").of(tacos);
        PlanItem salsa = new PlanItem("salsa").of(tacos);

        PlanItem lettuce = new PlanItem("lettuce").of(salad);
        PlanItem dressing = new PlanItem("dressing").of(salad);
        PlanItem chicken = new PlanItem("chicken").of(salad);

        // oh, we need cheese too
        PlanItem cheese = new PlanItem("cheese").of(tacos);

        PlanItem ham = new PlanItem("deli ham").of(lunch);
        PlanItem cheese2 = new PlanItem("cheese").of(lunch);
        PlanItem bread = new PlanItem("bread").of(lunch);

        System.out.println(renderTree("Ingredients", groceries));

        PlanItem costco = new PlanItem("Costco").of(groceries, null),
                winco = new PlanItem("Winco").of(groceries, costco);

        meat.of(winco);
        tortillas.after(meat);
        salsa.of(winco);
        lettuce.of(winco, null);
        dressing.of(winco, null);
        chicken.after(lettuce);
        cheese.of(costco);
        ham.after(lettuce);
        cheese.setName(cheese.getName() + " (2)");
        { // "delete" it
            cheese2.setParent(null);
            //noinspection UnusedAssignment
            cheese2 = null;
        }
        bread.after(salsa);

        System.out.println(renderTree("Shopping", groceries));
    }

    @Test
    public void taskCanBeItem() {
        RecipeBox box = new RecipeBox();
        PlanItem saltTask = new PlanItem("salt", box.salt);
        System.out.println(saltTask);
    }

    @Test
    public void trashBin() {
        val plan = new TaskList("The Plan");
        val a = new PlanItem("a");
        plan.addSubtask(a);
        val b = new PlanItem("b");
        a.addSubtask(b);
        val c = new PlanItem("c");
        b.addSubtask(c);

        System.out.println(renderTree("initial", plan));

        b.moveToTrash();
        System.out.println(renderTree("trash b", plan));

        a.moveToTrash();
        System.out.println(renderTree("trash a", plan));

        b.restoreFromTrash();
        System.out.println(renderTree("restore b", plan));

        a.restoreFromTrash();
        System.out.println(renderTree("restore a", plan));
    }

}
