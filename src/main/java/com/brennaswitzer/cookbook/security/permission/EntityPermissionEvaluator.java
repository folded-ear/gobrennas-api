package com.brennaswitzer.cookbook.security.permission;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.Owned;
import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.PlanBucket;
import com.brennaswitzer.cookbook.domain.PlanItem;
import com.brennaswitzer.cookbook.domain.PlanItemStatus;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * I check permissions on entities, by id. A target id may be a single id,
 * or a collection of ids (nested at most one level deep), every one of
 * which must be permitted. Null ids name no target, and are skipped. A
 * target type is the simple name of an entity class I know about.
 */
public class EntityPermissionEvaluator implements PermissionEvaluator {

    private static final List<Class<?>> TARGET_CLASSES = List.of(
            Plan.class,
            PlanBucket.class,
            PlanItem.class,
            PlannedRecipeHistory.class,
            Recipe.class,
            TextractJob.class);

    private final Map<String, Class<?>> targetClasses = TARGET_CLASSES
            .stream()
            .collect(Collectors.toMap(Class::getSimpleName,
                                      Function.identity()));

    private final ObjectProvider<EntityManager> entityManager;

    public EntityPermissionEvaluator(ObjectProvider<EntityManager> entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * I always deny: checks are always by id.
     */
    @Override
    public boolean hasPermission(Authentication auth,
                                 Object target,
                                 Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication auth,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (!(auth.getPrincipal() instanceof UserPrincipal principal)) {
            return false;
        }
        Class<?> targetClass = targetClasses.get(targetType);
        if (targetClass == null) {
            return false;
        }
        EntityManager em = entityManager.getObject();
        User user = em.getReference(User.class, principal.getId());
        return flatten(targetId)
                .filter(Objects::nonNull)
                .allMatch(id -> isPermitted(user,
                                            em.getReference(targetClass, id),
                                            permission));
    }

    private Stream<?> flatten(Object targetId) {
        if (targetId instanceof Collection<?> ids) {
            return ids.stream()
                    .flatMap(id -> id instanceof Collection<?> c
                            ? c.stream()
                            : Stream.of(id));
        }
        return Stream.of(targetId);
    }

    private boolean isPermitted(User user,
                                Object target,
                                Object permission) {
        if (permission instanceof String level) {
            return isPermitted(user, target, AccessLevel.valueOf(level));
        }
        if (permission instanceof PlanItemStatus status
            && target instanceof PlanItem item) {
            return item.isStatusPermitted(user, status);
        }
        return false;
    }

    private boolean isPermitted(User user,
                                Object target,
                                AccessLevel level) {
        if (target instanceof PlanItem item) {
            return item.getPlan().isPermitted(user, level);
        }
        if (target instanceof PlanBucket bucket) {
            return bucket.getPlan().isPermitted(user, level);
        }
        // an owner has every level of access
        if (target instanceof Owned o) return o.isOwner(user);
        return false;
    }

}
