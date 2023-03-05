package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.hibernate.SynchronizeableQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RecipeSearchRepositoryImpl implements RecipeSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PostgresFullTextQueryConverter queryConverter;

    private static class NativeQueryBuilder extends NamedParameterQuery {

        private final EntityManager entityManager;
        private final Class<?> resultClass;

        public NativeQueryBuilder(EntityManager entityManager,
                                  Class<?> resultClass) {
            this.entityManager = entityManager;
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
    public Slice<Recipe> searchRecipes(User user,
                                       String filter,
                                       Pageable pageable) {
        return searchRecipesByOwner(user, null, filter, pageable);
    }

    @Override
    public Slice<Recipe> searchRecipesByOwner(User user,
                                              Collection<User> owners,
                                              String filter,
                                              Pageable pageable) {
        NativeQueryBuilder builder = new NativeQueryBuilder(entityManager,
                                                            Recipe.class);

        boolean hasFilter = filter != null && !filter.isBlank();
        builder.append("SELECT ing.*\n" +
                               "FROM ingredient ing\n" +
                               "     LEFT JOIN favorite fav\n" +
                               "               ON fav.object_id = ing.id\n" +
                               "                   AND fav.object_type = ing.dtype\n" +
                               "                   AND fav.owner_id = :userId\n",
                       "userId",
                       user.getId());
        if (hasFilter) {
            builder.append("   , TO_TSQUERY('en', :query) query\n",
                           "query",
                           queryConverter.convert(filter));
        }
        builder.append("WHERE ing.dtype = 'Recipe'\n");
        if (hasFilter) {
            builder.append("  AND ing.recipe_fulltext @@ query\n");
        }
        if (owners != null && !owners.isEmpty()) {
            builder.append("  AND ing.owner_id in (:ownerIds)\n",
                           "ownerIds",
                           owners.stream()
                                   .map(User::getId)
                                   .collect(Collectors.toSet()));
        }
        builder.append("ORDER BY fav.id IS NULL\n");
        if (hasFilter) {
            builder.append("       , TS_RANK(recipe_fulltext, query) DESC\n");
        }
        builder.append("       , LOWER(ing.name)\n" +
                               "       , ing.id\n");

        return executeAndSlice(builder.build(), pageable);
    }

    /**
     * I provide the magic that Spring Data JPA does for a Slice return type on
     * a repository method, to execute an unbounded CriteriaQuery into a slice
     * of its result.
     *
     * @param query    The query to take a slice of results
     * @param pageable Where the slice should be taken
     * @param <T>      The result type of the query
     * @return The requested slice of the passed query.
     */
    private <T> Slice<T> executeAndSlice(Query query,
                                         Pageable pageable) {
        // copied from SlicedExecution in JpaQueryExecution

        int pageSize = pageable.getPageSize();
        query.setMaxResults(pageSize + 1);

        // this part wasn't in SlicedExecutions for reasons I don't understand
        int pageNumber = pageable.getPageNumber();
        if (pageNumber > 0) {
            query.setFirstResult(pageNumber * pageSize);
        }

        @SuppressWarnings("unchecked")
        List<T> resultList = (List<T>) query.getResultList();
        boolean hasNext = resultList.size() > pageSize;

        return new SliceImpl<>(hasNext
                                   ? resultList.subList(0, pageSize)
                                   : resultList,
                               pageable,
                               hasNext);
    }

}
