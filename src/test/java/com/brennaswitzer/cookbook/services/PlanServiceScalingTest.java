package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.PlanItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanServiceScalingTest {
    @InjectMocks private PlanService service;
    @Mock private PlanItemRepository itemRepo;

    private PlanItem recipe() {
        PlanItem root = new PlanItem("Edited chili", new Recipe("Chili"));
        root.setQuantity(Quantity.count(2));
        when(itemRepo.getReferenceById(1L)).thenReturn(root);
        return root;
    }

    @Test void scalesEditedQuantitiesOnceAndPreservesEverythingElse() {
        PlanItem root = recipe();
        PlanItem beans = new PlanItem("5 beans, rinsed", Quantity.count(5), new PantryItem("beans"), "rinsed");
        root.addAggregateComponent(beans);
        PlanItem note = new PlanItem("salt to taste");
        root.addChild(note);
        root.setNotes("My cooking notes");
        service.scaleRecipe(1L, 3);
        assertEquals(3, root.getQuantity().getQuantity());
        assertEquals(7.5, beans.getQuantity().getQuantity());
        assertEquals("7.5 beans, rinsed", beans.getName());
        assertEquals("rinsed", beans.getPreparation());
        assertEquals("My cooking notes", root.getNotes());
        assertNull(note.getQuantity());
        assertEquals("salt to taste", note.getName());
        service.scaleRecipe(1L, 3);
        assertEquals(7.5, beans.getQuantity().getQuantity(), "retry must not scale twice");
    }

    @Test void followsMovedPreparationAndOwnedSectionsButNotSubrecipeContents() {
        PlanItem root = recipe();
        Recipe section = new Recipe("Prep");
        ((Recipe) root.getIngredient()).addOwnedSection(section);
        PlanItem prep = new PlanItem("Prep", section);
        root.addAggregateComponent(prep);
        PlanItem onion = new PlanItem("3 onions", Quantity.count(3), new PantryItem("onions"), null);
        prep.addAggregateComponent(onion);
        new Plan("Earlier day").addChild(prep);
        // Model the inverse relationship as Hibernate loads it from storage.
        org.springframework.test.util.ReflectionTestUtils.setField(root, "components", java.util.Set.of(prep));
        PlanItem sauce = new PlanItem("Sauce", Quantity.count(4), new Recipe("Sauce"), null);
        root.addAggregateComponent(sauce);
        PlanItem garlic = new PlanItem("2 garlic", Quantity.count(2), new PantryItem("garlic"), null);
        sauce.addAggregateComponent(garlic);
        service.scaleRecipe(1L, 1);
        assertEquals(1.5, onion.getQuantity().getQuantity());
        assertEquals(2, sauce.getQuantity().getQuantity());
        assertEquals(2, garlic.getQuantity().getQuantity());
    }

    @Test void rejectsInvalidScalesBeforeLoadingOrChangingData() {
        for (double scale : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> service.scaleRecipe(1L, scale));
        }
        verifyNoInteractions(itemRepo);
    }

    @Test void rejectsOverflowWithoutPartialChanges() {
        PlanItem root = recipe();
        PlanItem beans = new PlanItem("beans", Quantity.count(Double.MAX_VALUE), new PantryItem("beans"), null);
        root.addChild(beans);
        assertThrows(IllegalArgumentException.class, () -> service.scaleRecipe(1L, 4));
        assertEquals(2, root.getQuantity().getQuantity());
        assertEquals(Double.MAX_VALUE, beans.getQuantity().getQuantity());
    }

    @Test void rejectsFinishedAndLinkedRecipes() {
        PlanItem root = recipe();
        root.setStatus(PlanItemStatus.COMPLETED);
        assertThrows(IllegalArgumentException.class, () -> service.scaleRecipe(1L, 3));
        root.setStatus(PlanItemStatus.NEEDED);
        new PlanItem("Parent").addAggregateComponent(root);
        assertThrows(IllegalArgumentException.class, () -> service.scaleRecipe(1L, 3));
        assertEquals(2, root.getQuantity().getQuantity());
    }
}
