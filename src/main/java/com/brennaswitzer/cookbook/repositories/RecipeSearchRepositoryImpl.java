package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.util.NamedParameterQuery;
import org.hibernate.SynchronizeableQuery;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

import static java.util.Collections.singleton;

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
    public SearchResponse<Recipe> searchRecipes(LibrarySearchRequest request) {
        NativeQueryBuilder builder = new NativeQueryBuilder(entityManager,
                                                            Recipe.class);

        builder.append("SELECT ing.*\n" +
                               "FROM ingredient ing\n" +
                               "     LEFT JOIN favorite fav\n" +
                               "               ON fav.object_id = ing.id\n" +
                               "                   AND fav.object_type = ing.dtype\n" +
                               "                   AND fav.owner_id = :userId\n",
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
        if (request.isOwnerConstrained()) {
            builder.append("  AND ing.owner_id in (:ownerIds)\n",
                           "ownerIds",
                           singleton(request.getUser().getId()));
        }
        builder.append("ORDER BY fav.id IS NULL\n");
        if (request.isFiltered()) {
            builder.append("       , TS_RANK(recipe_fulltext, query) DESC\n");
        }
        builder.append("       , LOWER(ing.name)\n" +
                               "       , ing.id\n");
        Query query = builder.build();

        query.setMaxResults(request.getLimit() + 1);
        if (request.isOffset()) {
            query.setFirstResult(request.getOffset());
        }
        @SuppressWarnings("unchecked")
        List<Recipe> resultList = (List<Recipe>) query.getResultList();

        return SearchResponse.of(request, resultList);
    }

}
