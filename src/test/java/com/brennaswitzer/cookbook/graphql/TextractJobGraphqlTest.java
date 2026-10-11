package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import com.brennaswitzer.cookbook.util.WithAliceBobEve;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.graphql.execution.GraphQlSource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@WithAliceBobEve
class TextractJobGraphqlTest {

    private static final String JOB_BY_ID = """
            query ($id: ID!) {
              textract {
                jobById(id: $id) { status ready errorMessage retryable }
              }
            }""";

    private static final String RETRY_JOB = """
            mutation ($id: ID!) {
              textract {
                retryJob(id: $id) { status ready errorMessage retryable }
              }
            }""";

    @Autowired GraphQlSource source;
    @Autowired EntityManager em;
    @Autowired UserPrincipalAccess principal;

    private TextractJob job;

    @BeforeEach
    void setUp() {
        job = new TextractJob();
        job.setOwner(principal.getUser());
        job.setPhoto(new S3File("photo", "image/jpeg", 1L));
        em.persist(job);
    }

    @Test
    void pendingJob() {
        Map<?, ?> info = jobById();

        assertEquals("PENDING", info.get("status"));
        assertEquals(false, info.get("ready"));
        assertNull(info.get("errorMessage"));
        assertEquals(false, info.get("retryable"));
    }

    @Test
    void readyJob() {
        job.succeed(Set.of());

        Map<?, ?> info = jobById();

        assertEquals("READY", info.get("status"));
        assertEquals(true, info.get("ready"));
        assertNull(info.get("errorMessage"));
        assertEquals(false, info.get("retryable"));
    }

    @Test
    void failedJob() {
        job.fail("Rate exceeded", true);

        Map<?, ?> info = jobById();

        assertEquals("ERROR", info.get("status"));
        assertEquals(false, info.get("ready"));
        assertEquals("Rate exceeded", info.get("errorMessage"));
        assertEquals(true, info.get("retryable"));
    }

    @Test
    void retryJobReturnsThePendingJob() {
        job.fail("Rate exceeded", true);

        ExecutionResult result = execute(RETRY_JOB);

        assertEquals(List.of(), result.getErrors());
        Map<?, ?> info = job(result, "retryJob");
        assertEquals("PENDING", info.get("status"));
        assertEquals(false, info.get("ready"));
        assertNull(info.get("errorMessage"));
        assertEquals(false, info.get("retryable"));
    }

    @Test
    void retryJobRejectsANonRetryableJob() {
        job.fail("Bad document", false);

        ExecutionResult result = execute(RETRY_JOB);

        assertEquals(List.of(ErrorType.BAD_REQUEST),
                     result.getErrors()
                             .stream()
                             .map(GraphQLError::getErrorType)
                             .toList());
    }

    private Map<?, ?> jobById() {
        ExecutionResult result = execute(JOB_BY_ID);
        assertEquals(List.of(), result.getErrors());
        return job(result, "jobById");
    }

    private ExecutionResult execute(String document) {
        em.flush();
        return source.graphQl()
                .execute(ExecutionInput.newExecutionInput()
                                 .query(document)
                                 .variables(Map.of("id", job.getId().toString()))
                                 .build());
    }

    private Map<?, ?> job(ExecutionResult result, String field) {
        Map<?, ?> data = result.getData();
        return (Map<?, ?>) ((Map<?, ?>) data.get("textract")).get(field);
    }

}
