package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import org.hibernate.SynchronizeableQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RecipeSearchRepositoryImplTest {

    @InjectMocks
    private RecipeSearchRepositoryImpl repo;

    @Mock
    private EntityManager entityManager;

    @Mock
    private PostgresFullTextQueryConverter queryConverter;

    @Mock
    private Query query;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<String> queryCaptor;

    @BeforeEach
    public void setup() {
        doReturn(mock(SynchronizeableQuery.class))
            .when(query)
            .unwrap(eq(SynchronizeableQuery.class));
        doReturn(query)
            .when(entityManager)
            .createNativeQuery(any(), eq(Recipe.class));
    }

    @Test
    public void pagingShenanigans() {
        doReturn(List.of(new Recipe(), new Recipe(), new Recipe()))
            .when(query)
            .getResultList();

        Slice<Recipe> result = repo.searchRecipes(null, PageRequest.of(3, 2));

        verify(query)
            .setFirstResult(6);
        verify(query)
            .setMaxResults(2 + 1);
        assertTrue(result.hasPrevious());
        assertTrue(result.hasNext());
        assertEquals(2, result.getSize());
    }

    @Test
    public void noOwnerNoFilter() {
        repo.searchRecipes(null, PageRequest.of(0, 2));

        verify(entityManager)
            .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.SELECT_ALL));
        assertFalse(sql.contains(RecipeSearchRepositoryImpl.OWNER_CLAUSE));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.ORDER_BY_ALL));
    }

    @Test
    public void noOwnerWithFilter() {
        String filter = "a neat filter";
        String tsquery = "parsed to query";
        doReturn(tsquery)
                .when(queryConverter)
                .convert(filter);

        repo.searchRecipes(filter, PageRequest.of(0, 2));

        verify(entityManager)
            .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
            .setParameter(eq("query"), queryCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.SELECT_FULLTEXT));
        assertFalse(sql.contains(RecipeSearchRepositoryImpl.OWNER_CLAUSE));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.ORDER_BY_FULLTEXT));
        assertEquals(tsquery, queryCaptor.getValue());
    }

    @Test
    public void ownerNoFilter() {
        User owner = mock(User.class);
        doReturn(2L)
            .when(owner)
            .getId();

        repo.searchRecipesByOwner(List.of(owner), "", PageRequest.of(0, 2));

        verify(entityManager)
            .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
            .setParameter(eq("ownerIds"), eq(Set.of(2L)));
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.SELECT_ALL));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.OWNER_CLAUSE));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.ORDER_BY_ALL));
    }

    @Test
    public void ownerWithFilter() {
        User owner = mock(User.class);
        doReturn(2L)
            .when(owner)
            .getId();

        repo.searchRecipesByOwner(List.of(owner), "chicken thighs", PageRequest.of(0, 2));

        verify(entityManager)
            .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
            .setParameter(eq("ownerIds"), eq(Set.of(2L)));
        verify(query)
            .setParameter(eq("query"), queryCaptor.capture());
        String sql = sqlCaptor.getValue();
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.SELECT_FULLTEXT));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.OWNER_CLAUSE));
        assertTrue(sql.contains(RecipeSearchRepositoryImpl.ORDER_BY_FULLTEXT));
    }

}
