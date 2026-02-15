package com.brennaswitzer.cookbook.graphql;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalExceptionHandler {

    @GraphQlExceptionHandler
    public GraphQLError handle(GraphqlErrorBuilder<?> errorBuilder,
                               EntityNotFoundException enfe) {
        return errorBuilder
                .errorType(ErrorType.BAD_REQUEST)
                .message(enfe.getMessage())
                .build();
    }

}
