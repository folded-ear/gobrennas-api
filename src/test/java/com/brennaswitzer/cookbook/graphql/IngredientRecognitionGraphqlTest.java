package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithAliceBobEve
class IngredientRecognitionGraphqlTest {
    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    @Test
    void groupedSuggestionsAndExplicitChoice() {
        PantryItem pantry = new PantryItem("stock");
        em.persist(pantry);
        Recipe parent = recipe("Soup");
        Recipe homemade = recipe("stock");
        Recipe section = recipe("stock");
        parent.addOwnedSection(section);
        em.flush();

        var suggested = source.graphQl().execute("""
                { library { recognizeItem(raw: "sto") {
                    suggestions(grouped: true) { name kind detail target { id start end } }
                } } }
                """);
        assertEquals(List.of(), suggested.getErrors());
        assertEquals(Map.of("library", Map.of("recognizeItem", Map.of("suggestions", List.of(
                suggestion(pantry.getId(), "PANTRY_ITEM", null),
                suggestion(homemade.getId(), "RECIPE", null),
                suggestion(section.getId(), "SECTION", "Soup")
        )))), suggested.getData());

        var recognized = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query("""
                    query($id: ID!) { library { recognizeItem(raw: "stock", choice: {id: $id, start: 0, end: 5}) {
                        ranges { type id start end }
                    } } }
                    """)
                .variables(Map.of("id", section.getId().toString())).build());
        assertEquals(List.of(), recognized.getErrors());
        assertEquals(Map.of("library", Map.of("recognizeItem", Map.of("ranges", List.of(
                Map.of("type", "ITEM", "id", section.getId().toString(), "start", 0, "end", 5)
        )))), recognized.getData());


    }

    @Test
    void legacyClientQueryPreservesRecognitionAndTextOnlySuggestions() {
        PantryItem pantry = new PantryItem("stock");
        em.persist(pantry);
        Recipe parent = recipe("Soup");
        recipe("stock");
        Recipe otherRecipe = recipe("stock soup");
        parent.addOwnedSection(recipe("stock"));
        parent.addOwnedSection(recipe("stock base"));
        em.flush();

        // Copied from gobrennas-client's GET_RECOGNIZED_ITEM and recogRange fragment.
        String query = """
                query recognizeItem($raw: String!, $cursor: NonNegativeInt) {
                  library {
                    recognizeItem(raw: $raw, cursor: $cursor) {
                      raw
                      cursor
                      ranges {
                        ...recogRange
                        quantity
                      }
                      suggestions {
                        name
                        target {
                          ...recogRange
                        }
                      }
                    }
                  }
                }
                fragment recogRange on RecognizedRange {
                  start
                  end
                  type
                  id
                }
                """;
        Map<String, Object> nullCursor = new java.util.HashMap<>();
        nullCursor.put("raw", "stock");
        nullCursor.put("cursor", null);
        List<Map<String, Object>> requests = List.of(
                Map.of("raw", "stock"), nullCursor, Map.of("raw", "stock", "cursor", 3));
        for (var variables : requests) {
            int cursor = variables.get("cursor") == null ? 5 : (int) variables.get("cursor");
            var response = source.graphQl().execute(ExecutionInput.newExecutionInput()
                    .query(query).variables(variables).build());
            assertEquals(List.of(), response.getErrors());
            Map<String, Object> range = new java.util.HashMap<>(
                    Map.of("start", 0, "end", 5, "type", "ITEM", "id", pantry.getId().toString()));
            range.put("quantity", null);
            assertEquals(Map.of("library", Map.of("recognizeItem", Map.of(
                    "raw", "stock", "cursor", cursor, "ranges", List.of(range),
                    "suggestions", List.of(
                            legacySuggestion(pantry.getId(), "stock", cursor),
                            legacySuggestion(otherRecipe.getId(), "stock soup", cursor))
            ))), response.getData());

            // Explicit null choice and false grouped mode have the same defaults.
            var explicitDefaults = source.graphQl().execute(ExecutionInput.newExecutionInput()
                    .query(query.replace("cursor: $cursor)", "cursor: $cursor, choice: null)")
                            .replace("suggestions {", "suggestions(grouped: false) {"))
                    .variables(variables).build());
            assertEquals(List.of(), explicitDefaults.getErrors());
            assertEquals(response.<Map<String, Object>>getData(), explicitDefaults.getData());
        }
    }

    @Test
    void sectionIngredientKeepsItsPositionAcrossCreateReadAndUpdate() {
        PantryItem flour = new PantryItem("flour");
        PantryItem salt = new PantryItem("salt");
        em.persist(flour);
        em.persist(salt);
        Recipe parent = recipe("Pie");
        Recipe filling = recipe("Filling");
        parent.addOwnedSection(filling);
        em.flush();

        List<Map<String, Object>> rows = List.of(
                Map.of("raw", "flour", "ingredientId", flour.getId().toString()),
                Map.of("raw", "Filling", "ingredientId", filling.getId().toString()),
                Map.of("raw", "salt", "ingredientId", salt.getId().toString()));
        var saved = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query("""
                    mutation($rows: [IngredientRefInfo!]!) { library { createRecipe(info: {
                        type: "Recipe", name: "New pie", ingredients: $rows
                    }, cookThis: false) { id } } }
                    """)
                .variables(Map.of("rows", rows)).build());
        assertEquals(List.of(), saved.getErrors());
        Map<String, Map<String, Map<String, Object>>> created = saved.getData();
        String id = (String) created.get("library").get("createRecipe").get("id");
        em.flush();
        em.clear();

        var loaded = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query("""
                    query($id: ID!) { library { getRecipeById(id: $id) {
                        ingredients { raw ingredient { id } } sections { id }
                    } } }
                    """)
                .variables(Map.of("id", id)).build());
        assertEquals(List.of(), loaded.getErrors());
        var expected = Map.of("ingredients", List.of(
                Map.of("raw", "flour", "ingredient", Map.of("id", flour.getId().toString())),
                Map.of("raw", "Filling", "ingredient", Map.of("id", filling.getId().toString())),
                Map.of("raw", "salt", "ingredient", Map.of("id", salt.getId().toString()))
        ), "sections", List.of());
        assertEquals(Map.of("library", Map.of("getRecipeById", expected)), loaded.getData());

        // Build the update from what an editor reads, rather than resending the original input.
        Map<String, Map<String, Map<String, Object>>> data = loaded.getData();
        var readRecipe = data.get("library").get("getRecipeById");
        var readRows = (List<?>) readRecipe.get("ingredients");
        var updatedRows = readRows.stream().map(value -> {
            var row = (Map<?, ?>) value;
            var ingredient = (Map<?, ?>) row.get("ingredient");
            return Map.of("raw", row.get("raw"), "ingredientId", ingredient.get("id"));
        }).toList();
        var updated = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query("""
                    mutation($id: ID!, $info: IngredientInfo!) { library { updateRecipe(id: $id, info: $info) {
                        ingredients { raw ingredient { id } } sections { id }
                    } } }
                    """)
                .variables(Map.of("id", id, "info", Map.of(
                        "type", "Recipe", "name", "Renamed pie", "ingredients", updatedRows,
                        "sections", readRecipe.get("sections")))).build());
        assertEquals(List.of(), updated.getErrors());
        assertEquals(Map.of("library", Map.of("updateRecipe", expected)), updated.getData());
        em.flush();
        em.clear();
        assertEquals(parent.getId(), em.find(Recipe.class, filling.getId()).getSectionOf().getId());
    }

    private Map<String, Object> legacySuggestion(Long id, String name, int end) {
        return Map.of("name", name, "target", Map.of(
                "start", 0, "end", end, "type", "ITEM", "id", id.toString()));
    }

    private Recipe recipe(String name) {
        Recipe result = new Recipe();
        result.setName(name);
        result.setOwner(principal.getUser());
        em.persist(result);
        return result;
    }

    private Map<String, Object> suggestion(Long id, String kind, String detail) {
        Map<String, Object> value = new java.util.HashMap<>();
        value.put("name", "stock");
        value.put("kind", kind);
        value.put("detail", detail);
        value.put("target", Map.of("id", id.toString(), "start", 0, "end", 3));
        return value;
    }
}
