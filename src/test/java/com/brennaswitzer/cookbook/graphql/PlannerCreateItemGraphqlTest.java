package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.UnitOfMeasure;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.graphql.execution.GraphQlSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@WithAliceBobEve
class PlannerCreateItemGraphqlTest {
    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private Plan plan;
    private PantryItem pantry;
    private Recipe recipe;
    private Recipe section;
    private String legacyMutation;

    @BeforeEach
    void setUp() throws IOException {
        legacyMutation = new ClassPathResource("graphql/legacy-create-plan-item.graphql")
                .getContentAsString(StandardCharsets.UTF_8);
        plan = new Plan(principal.getUser(), "This week");
        em.persist(plan);
        pantry = new PantryItem("stock");
        em.persist(pantry);
        em.persist(new UnitOfMeasure("cup"));
        recipe = recipe("stock");
        Recipe soup = recipe("Soup");
        section = recipe("stock");
        soup.addOwnedSection(section);
        em.flush();
    }

    @ParameterizedTest
    @ValueSource(strings = {"legacy", "literal null", "omitted variable", "null variable"})
    void noChoicePreservesTheLegacyRequestAndRecognition(String mode) {
        String raw = "1 1/2 cups stock, reduced";
        var variables = variables(raw);
        String mutation = legacyMutation;
        if (mode.equals("literal null")) {
            mutation = mutation.replace("name: $name)", "name: $name, choice: null)");
        } else if (!mode.equals("legacy")) {
            mutation = choiceMutation();
            if (mode.equals("null variable")) variables.put("choice", null);
        }

        var item = created(execute(mutation, variables));

        assertEquals(raw, item.get("name"));
        assertEquals(Map.of("id", pantry.getId().toString()), item.get("ingredient"));
        assertEquals("reduced", item.get("preparation"));
        assertEquals("NEEDED", item.get("status"));
        assertNull(item.get("notes"));
        assertNull(item.get("aggregate"));
        assertNull(item.get("bucket"));
        assertEquals(List.of(), item.get("children"));
        assertEquals(List.of(), item.get("components"));
        assertEquals(Map.of("id", plan.getId().toString()), item.get("parent"));
        assertQuantity(item, 1.5, "cup");
        Map<?, ?> parent = (Map<?, ?>) item.get("fullParent");
        assertEquals(plan.getId().toString(), parent.get("id"));
        assertEquals(List.of(Map.of("id", item.get("id"))), parent.get("children"));
        assertPersisted(item, pantry, raw);
    }

    @ParameterizedTest
    @ValueSource(strings = {"omitted", "null", "sibling"})
    void legacyPlacementStillWorks(String after) {
        PlanItem first = new PlanItem("!first").of(plan);
        em.persist(first);
        PlanItem last = new PlanItem("!last").of(plan);
        em.persist(last);
        em.flush();
        var variables = variables("stock");
        if (after.equals("null")) variables.put("afterId", null);
        if (after.equals("sibling")) variables.put("afterId", first.getId().toString());

        var item = created(execute(legacyMutation, variables));

        Map<?, ?> parent = (Map<?, ?>) item.get("fullParent");
        assertEquals(after.equals("sibling")
                     ? List.of(Map.of("id", first.getId().toString()), Map.of("id", item.get("id")),
                               Map.of("id", last.getId().toString()))
                     // Existing behavior inserts first when afterId is omitted or null.
                     : List.of(Map.of("id", item.get("id")), Map.of("id", first.getId().toString()),
                               Map.of("id", last.getId().toString())), parent.get("children"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pantry", "recipe", "section"})
    void explicitChoicePersistsTheExactIdentityWithQuantityAndPreparation(String kind) {
        Ingredient selected = switch (kind) {
            case "pantry" -> pantry;
            case "recipe" -> recipe;
            default -> section;
        };
        String raw = "2 cups stock, reduced";
        var variables = variables(raw);
        variables.put("choice", choice(selected.getId(), 7, 12));

        var item = created(execute(choiceMutation(), variables));

        assertEquals(Map.of("id", selected.getId().toString()), item.get("ingredient"));
        assertQuantity(item, 2.0, "cup");
        assertEquals("reduced", item.get("preparation"));
        assertEquals(List.of(), item.get("children"));
        assertPersisted(item, selected, raw);
    }

    @Test
    void quantityInsideASelectedRecipeNameIsNotParsedAsAnAmount() {
        Recipe burger = recipe("1/2-pound burger");
        String raw = "2 " + burger.getName() + ", grilled";
        var variables = variables(raw);
        variables.put("choice", choice(burger.getId(), 2, 2 + burger.getName().length()));

        var item = created(execute(choiceMutation(), variables));

        Map<?, ?> quantity = (Map<?, ?>) item.get("quantity");
        assertEquals(2.0, quantity.get("quantity"));
        assertNull(quantity.get("units"));
        assertEquals("grilled", item.get("preparation"));
        assertPersisted(item, burger, raw);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void leadingExclamationStillDisablesRecognition(boolean withChoice) {
        String raw = "!2 cups stock";
        var variables = variables(raw);
        if (withChoice) variables.put("choice", choice(recipe.getId(), 8, 13));

        var item = created(execute(withChoice ? choiceMutation() : legacyMutation, variables));

        assertEquals(raw, item.get("name"));
        assertNull(item.get("ingredient"));
        assertNull(item.get("quantity"));
        assertNull(item.get("preparation"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown ID", "wrong text", "outside text", "reversed", "empty", "negative", "missing ID"})
    void invalidChoiceCreatesNothing(String invalid) {
        var variables = variables("2 cups stock, reduced");
        var selection = switch (invalid) {
            case "unknown ID" -> choice(-1L, 7, 12);
            case "wrong text" -> choice(recipe.getId(), 0, 6);
            case "outside text" -> choice(recipe.getId(), 7, 100);
            case "reversed" -> choice(recipe.getId(), 12, 7);
            case "empty" -> choice(recipe.getId(), 7, 7);
            case "negative" -> choice(recipe.getId(), -1, 12);
            default -> Map.<String, Object>of("start", 7, "end", 12);
        };
        variables.put("choice", selection);
        long before = itemCount();

        var response = execute(choiceMutation(), variables);

        assertFalse(response.getErrors().isEmpty());
        assertEquals(before, itemCount());
        assertEquals(0, plan.getChildCount());
    }

    private String choiceMutation() {
        return legacyMutation.replace("$name: String!)", "$name: String!, $choice: RecognitionChoice)")
                .replace("name: $name)", "name: $name, choice: $choice)");
    }

    private Map<String, Object> variables(String name) {
        return new HashMap<>(Map.of("parentId", plan.getId().toString(), "name", name));
    }

    private Map<String, Object> choice(Long id, int start, int end) {
        return Map.of("id", id.toString(), "start", start, "end", end);
    }

    private ExecutionResult execute(String query, Map<String, Object> variables) {
        return source.graphQl().execute(ExecutionInput.newExecutionInput().query(query).variables(variables).build());
    }

    private Map<?, ?> created(ExecutionResult response) {
        assertEquals(List.of(), response.getErrors());
        Map<?, ?> data = response.getData();
        return (Map<?, ?>) ((Map<?, ?>) data.get("planner")).get("createItem");
    }

    private void assertQuantity(Map<?, ?> item, double amount, String unitName) {
        Map<?, ?> quantity = (Map<?, ?>) item.get("quantity");
        assertEquals(amount, quantity.get("quantity"));
        Map<?, ?> units = (Map<?, ?>) quantity.get("units");
        assertEquals(unitName, units.get("name"));
        assertNotNull(units.get("id"));
    }

    private void assertPersisted(Map<?, ?> item, Ingredient selected, String raw) {
        Long id = Long.valueOf((String) item.get("id"));
        Long ingredientId = selected.getId();
        em.flush();
        em.clear();
        PlanItem saved = em.find(PlanItem.class, id);
        assertEquals(ingredientId, saved.getIngredient().getId());
        assertEquals(raw, saved.getName());
    }

    private long itemCount() {
        return em.createQuery("select count(i) from PlanItem i", Long.class).getSingleResult();
    }

    private Recipe recipe(String name) {
        Recipe result = new Recipe();
        result.setName(name);
        result.setOwner(principal.getUser());
        em.persist(result);
        return result;
    }
}
