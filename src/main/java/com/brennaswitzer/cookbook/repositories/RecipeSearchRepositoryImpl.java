package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.SynchronizeableQuery;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.util.Collections.singleton;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RecipeSearchRepositoryImpl implements RecipeSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostgresFullTextQueryConverter queryConverter;

    @Autowired
    private UserPrincipalAccess principalAccess;

    private class NativeQueryBuilder extends NamedParameterQuery {

        private final Class<?> resultClass;

        public NativeQueryBuilder(Class<?> resultClass) {
            this.resultClass = resultClass;
        }

        public Query build() {
            Query query = entityManager.createNativeQuery(getStatement(),
                                                          resultClass);
            forEachParameter(query::setParameter);
            // You're forgiven for thinking Hibernate would know this...
            query.unwrap(SynchronizeableQuery.class)
                .addSynchronizedEntityClass(resultClass);
            return query;
        }

    }

    @Override
    public SearchResponse<Recipe> searchRecipes(LibrarySearchRequest request) {
        NativeQueryBuilder builder = new NativeQueryBuilder(Recipe.class);

        builder.append("""
                       SELECT ing.*
                       FROM ingredient ing
                            LEFT JOIN favorite fav
                                      ON fav.object_id = ing.id
                                          AND fav.object_type = ing.dtype
                                          AND fav.owner_id = :userId
                       """,
                       "userId",
                       request.getUser().getId());
        if (request.isFiltered()) {
            builder.append("   , TO_TSQUERY('en', :query) query\n",
                           "query",
                           queryConverter.convert(request.getFilter()));
        }
        builder.append("WHERE ing.dtype = 'Recipe'\n");
        if (request.isFiltered()) {
            builder.append("  AND ing.recipe_fulltext @@ query\n");
        }
        if (request.isIngredientConstrained()) {
            builder.append("""
                             AND EXISTS (
                                SELECT *
                                FROM recipe_ingredients ref
                                WHERE ref.recipe_id = ing.id
                                  AND ref.ingredient_id in :ingredientId
                             )
                           """, "ingredientId", request.getIngredientIds());
        }
        if (request.isOwnerConstrained()) {
            builder.append("  AND ing.owner_id in (:ownerIds)\n",
                           "ownerIds",
                           singleton(request.getUser().getId()));
        }
        builder.append("ORDER BY fav.id IS NULL\n");
        if (request.isFiltered()) {
            builder.append("       , TS_RANK(recipe_fulltext, query) DESC\n");
        }
        builder.append("""
                              , LOWER(ing.name)
                              , ing.id
                       """);
        Query query = builder.build();

        query.setMaxResults(request.getLimit() + 1);
        if (request.isOffset()) {
            query.setFirstResult(request.getOffset());
        }
        @SuppressWarnings("unchecked")
        List<Recipe> resultList = (List<Recipe>) query.getResultList();

        return SearchResponse.of(request, resultList);
    }

    @Override
    public long countTotalUses(PantryItem pantryItem) {
        if (pantryItem.getUseCount() != null) {
            return pantryItem.getUseCount();
        }
        return countUses(null, pantryItem);
    }

    @Override
    public long countMyUses(PantryItem pantryItem) {
        return countUses(principalAccess.getUser(), pantryItem);
    }

    private long countUses(User owner, PantryItem pantryItem) {
        var q = new NamedParameterQuery(
                """
                select count(distinct r.id)
                from Recipe r
                    join r.ingredients ing
                where ing.ingredient.id = :id
                """, "id", pantryItem.getId());
        if (owner != null) {
            q.append("  and r.owner.id = :owner",
                     "owner",
                     owner.getId());
        }
        var query = entityManager.createQuery(q.getStatement());
        q.forEachParameter(query::setParameter);
        return (Long) query.getResultList().iterator().next();
    }

}
