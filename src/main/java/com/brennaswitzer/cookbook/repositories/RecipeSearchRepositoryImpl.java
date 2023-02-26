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

    static final String SELECT_ALL = "SELECT *\n" +
        "FROM ingredient\n" +
        "WHERE dtype = 'Recipe'\n";

    static final String SELECT_FULLTEXT = "SELECT *\n" +
        "FROM ingredient\n" +
        "   , TO_TSQUERY('en', :query) query\n" +
        "WHERE dtype = 'Recipe'\n" +
        "  AND recipe_fulltext @@ query\n";

    static final String OWNER_CLAUSE = "  AND owner_id in (:ownerIds)\n";

    static final String ORDER_BY_ALL = "ORDER BY LOWER(name)\n" +
        "       , id";

    static final String ORDER_BY_FULLTEXT = "ORDER BY TS_RANK(recipe_fulltext, query) DESC\n" +
        "       , LOWER(name)\n" +
        "       , id";

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
    public Slice<Recipe> searchRecipes(String filter,
                                       Pageable pageable) {
        return searchRecipesByOwner(null, filter, pageable);
    }

    @Override
    public Slice<Recipe> searchRecipesByOwner(Collection<User> owners,
                                              String filter,
                                              Pageable pageable) {
        NativeQueryBuilder builder = new NativeQueryBuilder(entityManager,
                                                            Recipe.class);

        boolean selectAll = filter == null || filter.isBlank();
        if (selectAll) {
            builder.append(SELECT_ALL);
        } else {
            builder.append(SELECT_FULLTEXT,
                           "query",
                           queryConverter.convert(filter));
        }
        if (owners != null) {
            builder.append(OWNER_CLAUSE,
                           "ownerIds",
                           owners.stream()
                               .map(User::getId)
                               .collect(Collectors.toSet()));
        }
        if (selectAll) {
            builder.append(ORDER_BY_ALL);
        } else {
            builder.append(ORDER_BY_FULLTEXT);
        }

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
