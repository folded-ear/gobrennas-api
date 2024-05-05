package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.brennaswitzer.cookbook.util.PlanTestUtils.printTree;
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
    public void addChild_basics() {
        PlanItem groceries = new PlanItem("Groceries");
        assertFalse(groceries.isChild());
        assertFalse(groceries.hasChildren());
        assertEquals(0, groceries.getChildCount());

        PlanItem oj = new PlanItem("OJ");
        groceries.addChild(oj);

        assertFalse(groceries.isChild());
        assertTrue(groceries.hasChildren());
        assertEquals(1, groceries.getChildCount());

        assertTrue(oj.isChild());
        assertSame(groceries, oj.getParent());
        assertSame(oj, groceries.getChildView().iterator().next());
    }

    @Test
    public void addChild_ordering() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addChild(apples);
        groceries.addChild(oj);
        groceries.addChild(bagels);
        groceries.addChild(iceCream);

        assertBefore(apples, oj);
        assertBefore(oj, bagels);
        assertBefore(bagels, iceCream);
    }

    @Test
    public void addChildAfter() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addChild(oj);
        groceries.addChild(bagels);
        groceries.addChildAfter(apples, null);
        groceries.addChildAfter(iceCream, apples);

        assertBefore(apples, iceCream);
        assertBefore(iceCream, oj);
        assertBefore(oj, bagels);

        // oops, Ice Cream is after OJ

        groceries.addChildAfter(iceCream, oj);

        assertBefore(apples, oj);
        assertBefore(oj, iceCream);
        assertBefore(iceCream, bagels);
    }

    @Test
    public void insertChild() {
        PlanItem groceries = new PlanItem("Groceries");
        PlanItem apples = new PlanItem("Apples");
        PlanItem oj = new PlanItem("OJ");
        PlanItem bagels = new PlanItem("Bagels");
        PlanItem iceCream = new PlanItem("Ice Cream");
        groceries.addChild(apples);
        groceries.addChild(oj);
        groceries.addChild(bagels);
        groceries.insertChild(bagels.getPosition(), iceCream);

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
        groceries.addChild(apples);
        groceries.addChild(bagels);
        groceries.addChild(chicken);
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
        groceries.addChild(oj);

        assertEquals("Groceries", groceries.toString());
        assertEquals("OJ [Groceries]", oj.toString());

        PlanItem orange = new PlanItem("Orange");
        oj.addChild(orange);

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

        printTree("Meals", groceries);

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

        printTree("Ingredients", groceries);

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

        printTree("Shopping", groceries);
    }

    @Test
    public void planItemCanBePantryItem() {
        RecipeBox box = new RecipeBox();
        PlanItem saltPlanItem = new PlanItem("salt", box.salt);
        System.out.println(saltPlanItem);
    }

    @Test
    public void trashBin() {
        val plan = new Plan("The Plan");
        val a = new PlanItem("a");
        plan.addChild(a);
        val b = new PlanItem("b");
        a.addChild(b);
        val c = new PlanItem("c");
        b.addChild(c);

        printTree("initial", plan);

        b.moveToTrash();
        printTree("trash b", plan);

        a.moveToTrash();
        printTree("trash a", plan);

        b.restoreFromTrash();
        printTree("restore b", plan);

        a.restoreFromTrash();
        printTree("restore a", plan);
    }

}
