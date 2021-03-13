package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

public class RecipeSearchRepositoryImpl implements RecipeSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /*
        from Recipe r
        where lower(r.name) LIKE %:term%
            or lower(r.directions) LIKE %:term%
            or exists (
                select 1
                from r.labels l
                where lower(l.label.name) LIKE %:term%
            )
        order by case when lower(r.name) LIKE %:term% then 0 else 1 end
               , r.name
     */
    @Override
    public Slice<Recipe> searchRecipes(
            String term,
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipe> query = cb.createQuery(Recipe.class);
        Root<Recipe> recipeRoot = query.from(Recipe.class);
        query.select(recipeRoot);
        Expression<String> lowerName = cb.lower(recipeRoot.get(Recipe_.name));

        if (term == null || term.trim().isEmpty()) {
            query.orderBy(cb.asc(lowerName));
        } else {
            Expression<String> pattern = cb.literal('%' + term + '%');

            Subquery<Integer> labelSubquery = query.subquery(Integer.class);
            labelSubquery.select(cb.literal(1));
            Join<LabelRef, Label> labelJoin = labelSubquery
                    .correlate(recipeRoot)
                    .join(Recipe_.labels)
                    .join(LabelRef_.label);
            labelSubquery.where(
                    cb.like(cb.lower(labelJoin.get(Label_.name)), pattern)
            );

            Predicate nameMatch = cb.like(lowerName, pattern);
            query.where(cb.or(
                    nameMatch,
                    cb.like(cb.lower(recipeRoot.get(Recipe_.directions)), pattern),
                    cb.exists(labelSubquery)
            ));
            query.orderBy(
                    cb.asc(cb.selectCase()
                            .when(nameMatch, 0)
                            .otherwise(1)),
                    cb.asc(lowerName)
            );
        }

        return sliceQuery(query, pageable);
    }

    /**
     * I provide the magic that Spring Data JPA does for a Slice return type on
     * a repository method, to convert an unbounded CriteriaQuery into a slice.
     * @param query The query to take a slice of results
     * @param pageable Where the slice should be taken
     * @param <T> The result type of the query
     * @return The requested slice of the passed query.
     */
    private <T> Slice<T> sliceQuery(CriteriaQuery<T> query, Pageable pageable) {
        // copied from SlicedExecution in JpaQueryExecution
        TypedQuery<T> createQuery = entityManager.createQuery(query);

        int pageSize = pageable.getPageSize();
        createQuery.setMaxResults(pageSize + 1);

        // this part wasn't in SlicedExecutions for reasons I don't understand
        int pageNumber = pageable.getPageNumber();
        if (pageNumber > 0) {
            createQuery.setFirstResult(pageNumber * pageSize);
        }

        List<T> resultList = createQuery.getResultList();
        boolean hasNext = resultList.size() > pageSize;

        return new SliceImpl<>(hasNext ? resultList.subList(0, pageSize) : resultList, pageable, hasNext);
    }

}
