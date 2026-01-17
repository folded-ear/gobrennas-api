package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.MutableItem;
import com.brennaswitzer.cookbook.domain.Quantity;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.util.RecipeBox;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * I am really more of an integration test for recognition as a whole, in
 * contrast to {@link com.brennaswitzer.cookbook.util.RawUtilsTest} which is
 * only for context-free parsing of strings.
 */
@ExtendWith(MockitoExtension.class)
public class ItemServiceAutoRecogTest {

    @InjectMocks
    private ItemService service;

    @Mock
    private EntityManager entityManager;

    // Not every parameterization will end up using it this
    @Mock(strictness = Mock.Strictness.LENIENT)
    private IngredientService ingredientService;

    private static final RecipeBox box = new RecipeBox();

    @BeforeEach
    void setUp() {
        // sugar is known
        when(ingredientService.findAllIngredientsByNamesContaining(
                argThat(ns -> ns.contains(box.sugar.getName()))))
                .thenReturn(List.of(box.sugar));
        when(ingredientService.findIngredientByName(box.sugar.getName()))
                .thenReturn(Optional.of(box.sugar));
        when(ingredientService.ensureIngredientByName(box.sugar.getName()))
                .thenReturn(box.sugar);
        // chicken is not (though it _is_ in the RecipeBox)
        when(ingredientService.ensureIngredientByName(box.chicken.getName()))
                .thenReturn(box.chicken);
    }

    @ParameterizedTest
    @MethodSource("onePart")
    @MethodSource("twoParts")
    @MethodSource("threeParts")
    @MethodSource("createStuff")
    void combosAndPerms(Item expected) {
        Item actual = new Item(expected.getRaw());
        try (MockedStatic<UnitOfMeasure> uom = mockStatic(UnitOfMeasure.class)) {
            // cup is known
            uom.when(() -> UnitOfMeasure.find(entityManager, "cup"))
                    .thenReturn(Optional.of(box.cup));
            uom.when(() -> UnitOfMeasure.ensure(entityManager, "cup"))
                    .thenReturn(box.cup);
            // lbs is not (though it _is_ in the box)
            uom.when(() -> UnitOfMeasure.ensure(entityManager, "lbs"))
                    .thenReturn(box.lbs);

            service.autoRecognize(actual);
        }
        assertEquals(expected, actual);
    }

    private static Stream<Item> onePart() {
        return Stream.of(
                Item.builder()
                        .raw("5, divided")
                        .quantity(Quantity.count(5))
                        .preparation("divided")
                        .build(),
                // no unit w/out a quantity
                Item.builder()
                        .raw("cup, divided")
                        .build(),
                Item.builder()
                        .raw("sugar, divided")
                        .ingredient(box.sugar)
                        .preparation("divided")
                        .build()
        );
    }

    private static Stream<Item> twoParts() {
        return Stream.of(
                Item.builder()
                        .raw("5 cup, divided")
                        .quantity(new Quantity(5, box.cup))
                        .preparation("divided")
                        .build(),
                Item.builder()
                        .raw("5 sugar, divided")
                        .quantity(Quantity.count(5))
                        .ingredient(box.sugar)
                        .preparation("divided")
                        .build(),
                Item.builder()
                        .raw("cup 5, divided")
                        .build(),
                Item.builder()
                        .raw("cup sugar, divided")
                        .ingredient(box.sugar)
                        .preparation("cup, divided")
                        .build(),
                Item.builder()
                        .raw("sugar 5, divided")
                        .ingredient(box.sugar)
                        .preparation("5, divided")
                        .build(),
                Item.builder()
                        .raw("sugar cup, divided")
                        .ingredient(box.sugar)
                        .preparation("cup, divided")
                        .build()
        );
    }

    private static Stream<Item> threeParts() {
        return Stream.of(
                Item.builder()
                        .raw("5 cup sugar, divided")
                        .quantity(new Quantity(5, box.cup))
                        .ingredient(box.sugar)
                        .preparation("divided")
                        .build(),
                Item.builder()
                        .raw("5 sugar cup, divided")
                        .quantity(Quantity.count(5))
                        .ingredient(box.sugar)
                        .preparation("cup, divided")
                        .build(),
                Item.builder()
                        .raw("cup 5 sugar, divided")
                        .ingredient(box.sugar)
                        .preparation("cup 5, divided")
                        .build(),
                Item.builder()
                        .raw("cup sugar 5, divided")
                        .ingredient(box.sugar)
                        .preparation("cup 5, divided")
                        .build(),
                Item.builder()
                        .raw("sugar 5 cup, divided")
                        .ingredient(box.sugar)
                        .preparation("5 cup, divided")
                        .build(),
                Item.builder()
                        .raw("sugar cup 5, divided")
                        .ingredient(box.sugar)
                        .preparation("cup 5, divided")
                        .build()
        );
    }

    private static Stream<Item> createStuff() {
        return Stream.of(
                Item.builder()
                        .raw("1&1/2 lbs young chicken, deboned")
                        .quantity(Quantity.count(1.5))
                        .preparation("lbs young chicken, deboned")
                        .build(),
                Item.builder()
                        .raw("1&1/2 _lbs_ young chicken, deboned")
                        .quantity(new Quantity(1.5, box.lbs))
                        .preparation("young chicken, deboned")
                        .build(),
                Item.builder()
                        .raw("1&1/2 lbs young \"chicken\", deboned")
                        .quantity(Quantity.count(1.5))
                        .ingredient(box.chicken)
                        .preparation("lbs young, deboned")
                        .build(),
                Item.builder()
                        .raw("1&1/2 _lbs_ young \"chicken\", deboned")
                        .quantity(new Quantity(1.5, box.lbs))
                        .ingredient(box.chicken)
                        .preparation("young, deboned")
                        .build(),
                Item.builder()
                        .raw("_lbs_ chicken")
                        .quantity(new Quantity(1, box.lbs))
                        .preparation("chicken")
                        .build(),
                Item.builder()
                        .raw("_lbs_ 5 chicken")
                        .quantity(new Quantity(1, box.lbs))
                        .preparation("5 chicken")
                        .build(),
                Item.builder()
                        .raw("\"chicken\" 2 _lbs_")
                        .quantity(new Quantity(1, box.lbs))
                        .ingredient(box.chicken)
                        .preparation("2")
                        .build()
        );
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class Item implements MutableItem {

        private final String raw;
        private Quantity quantity;
        private String preparation;
        private Ingredient ingredient;

    }

}
