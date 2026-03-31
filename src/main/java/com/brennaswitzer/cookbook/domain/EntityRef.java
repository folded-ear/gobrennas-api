package com.brennaswitzer.cookbook.domain;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.hibernate.Hibernate;

@Embeddable
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityRef {

    @Setter(lombok.AccessLevel.NONE)
    @Nonnull
    @Column(name = "entity_type")
    String className;

    @Nonnull
    @Column(name = "entity_id")
    Long id;

    public static EntityRef from(BaseEntity entity) {
        EntityRef ref = new EntityRef();
        ref.setEntity(entity);
        return ref;
    }

    @Transient
    @SneakyThrows
    public <T extends BaseEntity> T getEntity() {
        T entity = this.<T>getEntityClass()
                .getDeclaredConstructor()
                .newInstance();
        entity.setId(id);
        return entity;
    }

    @Transient
    @SneakyThrows
    public <T extends BaseEntity> T getEntity(EntityManager entityManager) {
        return entityManager.getReference(getEntityClass(), id);
    }

    public <T extends BaseEntity> void setEntity(T entity) {
        id = entity.getId();
        className = Hibernate.unproxy(entity)
                .getClass()
                .getName();
    }

    @SneakyThrows
    private <T extends BaseEntity> @Nonnull Class<T> getEntityClass() {
        @SuppressWarnings("unchecked")
        Class<T> entityClass = (Class<T>) Class.forName(className);
        return entityClass;
    }

}
