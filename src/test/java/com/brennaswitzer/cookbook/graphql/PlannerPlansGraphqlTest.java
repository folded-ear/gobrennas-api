package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;

import java.util.List;
import java.util.Map;

import static com.brennaswitzer.cookbook.util.UserTestUtils.createUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithAliceBobEve
class PlannerPlansGraphqlTest {

    private static final String PLANS_BY_IDS = """
            query ($ids: [ID!]) {
              planner { plans(ids: $ids) { id name } }
            }""";
    private static final String ALL_PLANS = """
            query { planner { plans { id name } } }""";
    private static final String ONE_PLAN = """
            query ($id: ID!) {
              planner { plan(id: $id) { id name } }
            }""";

    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private Plan first;
    private Plan second;

    @BeforeEach
    void setUp() {
        first = new Plan(principal.getUser(), "First");
        em.persist(first);
        second = new Plan(principal.getUser(), "Second");
        em.persist(second);
        em.flush();
    }

    @Test
    void omittedIdsReturnsAllPlans() {
        var plans = plans(execute(ALL_PLANS, Map.of()));

        assertTrue(plans.contains(plan(first)));
        assertTrue(plans.contains(plan(second)));
    }

    @Test
    void idsReturnsThosePlansInOrder() {
        var plans = plans(execute(PLANS_BY_IDS,
                                  Map.of("ids",
                                         List.of(id(second), id(first)))));

        assertEquals(List.of(plan(second), plan(first)), plans);
    }

    @Test
    void singleIdMatchesPlan() {
        var one = execute(ONE_PLAN, Map.of("id", id(first)));
        assertEquals(List.of(), one.getErrors());
        Map<?, ?> planner = (Map<?, ?>) ((Map<?, ?>) one.getData())
                .get("planner");

        var plans = plans(execute(PLANS_BY_IDS,
                                  Map.of("ids", List.of(id(first)))));

        assertEquals(List.of(planner.get("plan")), plans);
    }

    @Test
    void inaccessibleIdFailsLikePlan() {
        User mallory = createUser("Mallory");
        em.persist(mallory);
        Plan theirs = new Plan(mallory, "Theirs");
        em.persist(theirs);
        em.flush();

        var one = execute(ONE_PLAN, Map.of("id", id(theirs)));
        var many = execute(PLANS_BY_IDS,
                           Map.of("ids", List.of(id(first), id(theirs))));

        assertFalse(one.getErrors().isEmpty());
        assertFalse(many.getErrors().isEmpty());
    }

    private ExecutionResult execute(String query,
                                    Map<String, Object> variables) {
        return source.graphQl()
                .execute(ExecutionInput.newExecutionInput()
                                 .query(query)
                                 .variables(variables)
                                 .build());
    }

    private List<?> plans(ExecutionResult response) {
        assertEquals(List.of(), response.getErrors());
        Map<?, ?> data = response.getData();
        return (List<?>) ((Map<?, ?>) data.get("planner")).get("plans");
    }

    private Map<String, String> plan(Plan p) {
        return Map.of("id", id(p), "name", p.getName());
    }

    private String id(Plan p) {
        return p.getId().toString();
    }

}
