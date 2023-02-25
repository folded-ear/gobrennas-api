package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.hibernate.SynchronizeableQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private PostgresFullTextQueryParser queryParser;

    // NOT thread safe
    private static class ParameterizedQueryBuilder {
        private final EntityManager entityManager;
        private final Class<?> resultClass;
        private final StringBuilder sql = new StringBuilder();
        private final Map<String, Object> params = new HashMap<>();

        public ParameterizedQueryBuilder(EntityManager entityManager,
                                         Class<?> resultClass) {
            this.entityManager = entityManager;
            this.resultClass = resultClass;
        }

        public ParameterizedQueryBuilder append(String sql) {
            this.sql.append(sql);
            return this;
        }

        public ParameterizedQueryBuilder append(String sql,
                                                Map<String, Object> params) {
            append(sql);
            params.forEach(this::addParam);
            return this;
        }

        public ParameterizedQueryBuilder append(String sql,
                                                String paramName,
                                                Object paramValue) {
            append(sql);
            addParam(paramName, paramValue);
            return this;
        }

        private void addParam(String paramName,
                              Object paramValue) {
            if (params.containsKey(paramName)) {
                throw new IllegalArgumentException(String.format("A '%s' parameter is already defined (set to %s).",
                                                                 paramName,
                                                                 params.get(paramName)));
            }
            params.put(paramName, paramValue);
        }

        public String getSql() {
            return sql.toString();
        }

        public Map<String, Object> getParameters() {
            return params;
        }

        public Query build() {
            Query query = entityManager.createNativeQuery(getSql(),
                                                          resultClass);
            getParameters().forEach(query::setParameter);
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
        ParameterizedQueryBuilder builder = new ParameterizedQueryBuilder(entityManager,
                                                                          Recipe.class);

        boolean selectAll = filter == null || filter.isBlank();
        if (selectAll) {
            builder.append(SELECT_ALL);
        } else {
            builder.append(SELECT_FULLTEXT,
                           "query",
                           queryParser.parse(filter));
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
