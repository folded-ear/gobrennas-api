package com.brennaswitzer.cookbook.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

@Embeddable
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class EntityRef {

    @Setter(lombok.AccessLevel.NONE)
    @Nonnull
    String entityClassName;

    @Nonnull
    Long entityId;

    @Transient
    @SneakyThrows
    public <T extends BaseEntity & AccessControlled> T getEntity() {
        T entity = this.<T>getEntityClass()
                .getDeclaredConstructor()
                .newInstance();
        entity.setId(entityId);
        return entity;
    }

    @Transient
    @SneakyThrows
    public <T extends BaseEntity & AccessControlled> T getEntity(EntityManager entityManager) {
        return entityManager.getReference(getEntityClass(), entityId);
    }

    public <T extends BaseEntity & AccessControlled> void setEntity(T entity) {
        entityId = entity.getId();
        entityClassName = Hibernate.unproxy(entity)
                .getClass()
                .getName();
    }

    @SneakyThrows
    private <T extends BaseEntity & AccessControlled> @Nonnull Class<T> getEntityClass() {
        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) Class.forName(entityClassName);
        return entityClass;
    }

}
