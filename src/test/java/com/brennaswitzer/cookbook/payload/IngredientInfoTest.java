package com.brennaswitzer.cookbook.payload;

import com.brennaswitzer.cookbook.domain.Label;
import com.brennaswitzer.cookbook.domain.Recipe;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IngredientInfoTest {

    Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe("Test");
        recipe.addLabel(new Label("chicken"));
        recipe.addLabel(new Label("dinner"));
    }

    @Test
    public void from() {
        IngredientInfo i = IngredientInfo.from(recipe);
        assertEquals(2, i.getLabels().size());
    }
}
