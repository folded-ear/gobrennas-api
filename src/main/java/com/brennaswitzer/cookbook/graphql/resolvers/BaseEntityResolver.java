package com.brennaswitzer.cookbook.graphql.resolvers;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import org.springframework.graphql.data.method.annotation.SchemaMapping;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public abstract class BaseEntityResolver<T extends BaseEntity> {

    @SchemaMapping
    public OffsetDateTime createdAt(T e) {
        return e.getCreatedAt()
                .atOffset(ZoneOffset.UTC);
    }

}
