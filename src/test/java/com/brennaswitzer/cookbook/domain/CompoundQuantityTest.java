package com.brennaswitzer.cookbook.domain;

import com.brennaswitzer.cookbook.util.RecipeBox;
import lombok.val;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class CompoundQuantityTest {

    @Test
    public void plus() {
        val one = CompoundQuantity.ONE.plus(CompoundQuantity.ZERO);
        val another = CompoundQuantity.ZERO.plus(CompoundQuantity.ONE);
        assertEquals(one, another);
        assertNotSame(one, another);
        assertNotSame(one.getComponents(), another.getComponents());
    }

    @Test
    public void lksdf() {
        val box = new RecipeBox();
        val salt = new InventoryItem(
                new User(),
                box.salt
        );

        // I _seriously_ bought salt
        salt.acquire(new Quantity(123456, box.cup));

        // bought some more in here too, but didn't write it down
        for (int i = 4; i <= 19; i++) {
            salt.consume(new Quantity(i, box.tbsp));
        }

        // I just checked; I have 3 cups of salt.
        salt.reset(new Quantity(3, box.cup));

        // used 4 Tbsp (half cup)
        salt.consume(new Quantity(4, box.tbsp));

        System.out.println(salt);
        assertEquals(new CompoundQuantity(new Quantity(2.75, box.cup)), salt.getQuantity());
    }

}
