package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import graphql.kickstart.tools.GraphQLResolver;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public abstract class BaseEntityResolver<T extends BaseEntity> implements GraphQLResolver<T> {

    public OffsetDateTime createdAt(T e) {
        return e.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
