package com.brennaswitzer.cookbook.graphql.support;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.LabelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Info2RecipeTest {

    private static final long USER_ID = 123L;

    @InjectMocks
    private Info2Recipe converter;

    @Mock
    private LabelService labelService;
    @Mock
    private EntityManager entityManager;
    @Mock
    private UserPrincipal userPrincipal;

    private IngredientInfo withSections;
    private AtomicLong id_seq;

    @BeforeEach
    void setUp() throws IOException {
        when(userPrincipal.getId())
                .thenReturn(USER_ID);
        doAnswer(iom -> {
            Class<? extends BaseEntity> clazz = iom.getArgument(0);
            Long id = iom.getArgument(1);
            if (Ingredient.class.equals(clazz)) {
                clazz = id < 100
                        ? Recipe.class
                        : PantryItem.class;
            }
            BaseEntity it = clazz.newInstance();
            it.setId(id);
            if (it instanceof Ingredient ing) {
                ing.setName("ing " + id);
            }
            return it;
        })
                .when(entityManager)
                .find(any(), any());
        id_seq = new AtomicLong(100000);
        doAnswer(iom -> {
            BaseEntity it = iom.getArgument(0);
            it.setId(id_seq.incrementAndGet());
            return null;
        })
                .when(entityManager)
                .persist(any());

        withSections = new ObjectMapper()
                .readValue(getClass().getResourceAsStream("with_sections.json"),
                           IngredientInfo.class);
    }

    @Test
    void topLevel() {
        Recipe recipe = converter.convert(userPrincipal,
                                          withSections);

        assertEquals("With Sections",
                     recipe.getName());
        assertEquals("First the flugel, then the booble",
                     recipe.getDirections());
        assertEquals(4 + 2,
                     recipe.getIngredients().size());
        // just the top-level recipe
        verify(entityManager).persist(any(Recipe.class));
        verify(labelService).updateLabels(recipe,
                                          List.of("top-level"));
    }

    @Test
    void unsavedSectionBecomesOwned() {
        withSections.getSections().get(1).setId(null);
        Recipe recipe = converter.convert(userPrincipal,
                                          withSections);

        assertEquals(1,
                     recipe.getOwnedSections().size());
        assertSame(recipe,
                   recipe.getOwnedSections().iterator().next().getSectionOf());
    }

    @Test
    void byReferenceSectionsDontUpdate() {
        withSections.getSections().get(1).setId(null);
        Recipe recipe = converter.convert(userPrincipal,
                                          withSections);

        List<IngredientRef> sections = recipe.getIngredients()
                .stream()
                .filter(IngredientRef::isSection)
                .toList();
        assertEquals(2,
                     sections.size());
        assertEquals(List.of("ing 48", "Italian Dressing"),
                     sections.stream()
                             .map(IngredientRef::getIngredient)
                             .map(Ingredient::getName)
                             .toList());
    }

    @Test
    void saveOwnedSectionsLabels() {
        withSections.getSections().get(1).setId(null);
        Recipe recipe = converter.convert(userPrincipal,
                                          withSections);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Ingredient dressing = recipe.getIngredients()
                .stream()
                .filter(IngredientRef::isSection)
                .skip(1)
                .map(IngredientRef::getIngredient)
                .findFirst()
                .get();
        verify(labelService).updateLabels(dressing,
                                          List.of("section", "dressing"));
    }

}
