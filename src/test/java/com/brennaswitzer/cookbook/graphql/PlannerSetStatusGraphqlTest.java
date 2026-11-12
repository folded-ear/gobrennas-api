package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Coordinates;
import com.brennaswitzer.cookbook.domain.IngredientAcquisition;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.repositories.IngredientAcquisitionRepository;
import com.brennaswitzer.cookbook.util.RecipeBox;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithAliceBobEve
class PlannerSetStatusGraphqlTest {

    private static final String ACQUIRE_ONE = """
            mutation ($id: ID!, $doneAt: DateTime, $coords: CoordinatesInput) {
              planner {
                setStatus(id: $id,
                          status: ACQUIRED,
                          doneAt: $doneAt,
                          coords: $coords) { id }
              }
            }""";
    private static final String ACQUIRE_TWO = """
            mutation ($a: ID!,
                      $b: ID!,
                      $doneAt: DateTime,
                      $coords: CoordinatesInput) {
              planner {
                a: setStatus(id: $a,
                             status: ACQUIRED,
                             doneAt: $doneAt,
                             coords: $coords) { id }
                b: setStatus(id: $b,
                             status: ACQUIRED,
                             doneAt: $doneAt,
                             coords: $coords) { id }
              }
            }""";
    private static final Instant DONE_AT = Instant.parse("2026-11-12T15:30:00Z");
    private static final Coordinates COORDS = new Coordinates(45.5, -122.6, 12);

    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;
    @Autowired IngredientAcquisitionRepository acquisitionRepo;

    private RecipeBox box;
    private PlanItem first;
    private PlanItem second;

    @BeforeEach
    void setUp() {
        box = new RecipeBox();
        box.persist(em, principal.getUser());
        Plan groceries = new Plan(principal.getUser(), "Groceries");
        em.persist(groceries);
        first = new PlanItem("salt", box.salt).of(groceries);
        em.persist(first);
        second = new PlanItem("more salt", box.salt).of(groceries);
        em.persist(second);
        em.flush();
    }

    @Test
    void sameIngredientTwiceInOneRequestRecordsOnce() {
        execute(ACQUIRE_TWO,
                Map.of("a", id(first),
                       "b", id(second),
                       "doneAt", DONE_AT.toString(),
                       "coords", coords()));

        var acquisitions = acquisitions();
        assertEquals(1, acquisitions.size());
        var acquisition = acquisitions.get(0);
        assertEquals(box.salt.getId(), acquisition.getIngredient().getId());
        assertEquals(DONE_AT, acquisition.getAcquiredAt());
        assertEquals(COORDS, acquisition.getCoordinates());
    }

    @Test
    void sameIngredientInTwoRequestsRecordsTwice() {
        execute(ACQUIRE_ONE,
                Map.of("id", id(first),
                       "doneAt", DONE_AT.toString(),
                       "coords", coords()));
        execute(ACQUIRE_ONE,
                Map.of("id", id(second),
                       "doneAt", DONE_AT.toString(),
                       "coords", coords()));

        assertEquals(2, acquisitions().size());
    }

    private void execute(String query, Map<String, Object> variables) {
        ExecutionResult result = source.graphQl()
                .execute(ExecutionInput.newExecutionInput()
                                 .query(query)
                                 .variables(variables)
                                 .build());
        assertEquals(List.of(), result.getErrors());
    }

    private List<IngredientAcquisition> acquisitions() {
        em.flush();
        em.clear();
        return acquisitionRepo.findAll();
    }

    private Map<String, Object> coords() {
        return Map.of("latitude", COORDS.latitude(),
                      "longitude", COORDS.longitude(),
                      "accuracy", COORDS.accuracy());
    }

    private String id(PlanItem item) {
        return item.getId().toString();
    }

}
