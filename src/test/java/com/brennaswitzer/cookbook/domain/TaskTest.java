package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.TaskTestUtils.renderTree;
import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    private void assertBefore(Task first, Task second) {
        assertTrue(
                first.getPosition() < second.getPosition(),
                first.getName() + " (" + first.getPosition() + ") is before " + second.getName() + " (" + second.getPosition() + ")"
        );
    }

    @Test
    public void addSubtask_basics() {
        Task groceries = new Task("Groceries");
        assertFalse(groceries.isSubtask());
        assertFalse(groceries.hasSubtasks());
        assertEquals(0, groceries.getSubtaskCount());

        Task oj = new Task("OJ");
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
        Task groceries = new Task("Groceries");
        Task apples = new Task("Apples");
        Task oj = new Task("OJ");
        Task bagels = new Task("Bagels");
        Task iceCream = new Task("Ice Cream");
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
        Task groceries = new Task("Groceries");
        Task apples = new Task("Apples");
        Task oj = new Task("OJ");
        Task bagels = new Task("Bagels");
        Task iceCream = new Task("Ice Cream");
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
        Task groceries = new Task("Groceries");
        Task apples = new Task("Apples");
        Task oj = new Task("OJ");
        Task bagels = new Task("Bagels");
        Task iceCream = new Task("Ice Cream");
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
        Task groceries = new Task("Groceries");
        Task apples = new Task("Apples");
        Task bagels = new Task("Bagels");
        Task chicken = new Task("Chicken");
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
        Task groceries = new Task("Groceries");
        assertEquals("Groceries", groceries.toString());

        Task oj = new Task("OJ");
        groceries.addSubtask(oj);

        assertEquals("Groceries", groceries.toString());
        assertEquals("OJ [Groceries]", oj.toString());

        Task orange = new Task("Orange");
        oj.addSubtask(orange);

        assertEquals("OJ [Groceries]", oj.toString());
        assertEquals("Orange [OJ]", orange.toString());
    }

    @Test
    public void BY_NAME() {
        Task a = new Task("a");
        assertEquals(0, Task.BY_NAME.compare(a, a));
        assertTrue(Task.BY_NAME.compare(a, null) < 0);
        assertTrue(Task.BY_NAME.compare(null, a) > 0);
        Task b = new Task("b");
        assertTrue(Task.BY_NAME.compare(a, b) < 0);
        assertTrue(Task.BY_NAME.compare(b, a) > 0);

        // UPPERCASE < lowercase
        Task B = new Task("B");
        assertTrue(Task.BY_NAME.compare(a, B) > 0);
        assertTrue(Task.BY_NAME.compare(B, a) < 0);
    }

    @Test
    public void BY_NAME_IGNORE_CASE() {
        Task a = new Task("a");
        assertEquals(0, Task.BY_NAME_IGNORE_CASE.compare(a, a));
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(a, null) < 0);
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(null, a) > 0);
        Task b = new Task("b");
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(a, b) < 0);
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(b, a) > 0);

        Task B = new Task("B");
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(a, B) < 0);
        assertTrue(Task.BY_NAME_IGNORE_CASE.compare(B, a) > 0);
    }

    @Test
    public void BY_ORDER() {
        Task a = new Task("", 1);
        assertEquals(0, Task.BY_ORDER.compare(a, a));
        assertTrue(Task.BY_ORDER.compare(a, null) < 0);
        assertTrue(Task.BY_ORDER.compare(null, a) > 0);
        Task b = new Task("", 2);
        assertTrue(Task.BY_ORDER.compare(a, b) < 0);
        assertTrue(Task.BY_ORDER.compare(b, a) > 0);
    }

    @Test
    public void muppetLikeListsForShopping() {
        Task groceries = new Task("Groceries");
        Task tacos = new Task("Tacos").of(groceries);
        Task salad = new Task("Salad").of(groceries);
        Task lunch = new Task("Lunch").of(groceries);

        System.out.println(renderTree("Meals", groceries));

        Task meat = new Task("meat").of(tacos);
        Task tortillas = new Task("tortillas").of(tacos);
        Task salsa = new Task("salsa").of(tacos);

        Task lettuce = new Task("lettuce").of(salad);
        Task dressing = new Task("dressing").of(salad);
        Task chicken = new Task("chicken").of(salad);

        // oh, we need cheese too
        Task cheese = new Task("cheese").of(tacos);

        Task ham = new Task("deli ham").of(lunch);
        Task cheese2 = new Task("cheese").of(lunch);
        Task bread = new Task("bread").of(lunch);

        System.out.println(renderTree("Ingredients", groceries));

        Task costco = new Task("Costco").of(groceries, null),
                winco = new Task("Winco").of(groceries, costco);

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
        Task saltTask = new Task("salt", box.salt);
        System.out.println(saltTask);
    }

}
