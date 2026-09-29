package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ErrorType;
import graphql.ExecutionInput;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class IngredientRecognitionGraphqlTest {
    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    @Test
    void groupedSuggestionsExplicitChoiceAndSectionSaveRoundTrip() {
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

        var saved = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query("""
                    mutation($id: ID!) { library { createRecipe(info: {
                        type: "Recipe", name: "New soup", ingredients: [{raw: "stock", ingredientId: $id, section: true}]
                    }, cookThis: false) { ingredients { raw } sections { id name } } } }
                    """)
                .variables(Map.of("id", section.getId().toString())).build());
        assertEquals(List.of(), saved.getErrors());
        assertEquals(Map.of("library", Map.of("createRecipe", Map.of(
                "ingredients", List.of(), "sections", List.of(Map.of("id", section.getId().toString(), "name", "stock"))
        ))), saved.getData());
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
    void sectionDefaultsToFalseAndRejectsExplicitNull() {
        Recipe ingredient = recipe("stock");
        em.flush();
        String mutation = """
                mutation($row: IngredientRefInfo!) { library { createRecipe(info: {
                    type: "Recipe", name: "Soup", ingredients: [$row]
                }, cookThis: false) { ingredients { raw } sections { id } } } }
                """;
        Map<String, Object> row = new java.util.HashMap<>(Map.of(
                "raw", "stock", "ingredientId", ingredient.getId().toString()));
        // Both omission and explicit false must keep this as an ordinary ingredient.
        for (int attempt = 0; attempt < 2; attempt++) {
            var saved = source.graphQl().execute(ExecutionInput.newExecutionInput()
                    .query(mutation).variables(Map.of("row", row)).build());
            assertEquals(List.of(), saved.getErrors());
            assertEquals(Map.of("library", Map.of("createRecipe", Map.of(
                    "ingredients", List.of(Map.of("raw", "stock")), "sections", List.of()
            ))), saved.getData());
            row.put("section", false);
        }

        row.put("section", null);
        var rejected = source.graphQl().execute(ExecutionInput.newExecutionInput()
                .query(mutation).variables(Map.of("row", row)).build());
        assertNull(rejected.getData());
        assertEquals(1, rejected.getErrors().size());
        assertEquals(ErrorType.ValidationError, rejected.getErrors().get(0).getErrorType());
        assertTrue(rejected.getErrors().get(0).getMessage().contains("section"));
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
