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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

    @Mock
    private User user;

    @BeforeEach
    public void setup() {
        doReturn(mock(SynchronizeableQuery.class))
                .when(query)
                .unwrap(eq(SynchronizeableQuery.class));
        doReturn(query)
                .when(entityManager)
                .createNativeQuery(any(), eq(Recipe.class));
        doReturn(2L)
                .when(user)
                .getId();
    }

    @Test
    public void pagingShenanigans() {
        doReturn(List.of(new Recipe(), new Recipe(), new Recipe()))
            .when(query)
            .getResultList();

        Slice<Recipe> result = repo.searchRecipes(user, null, PageRequest.of(3, 2));

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
        repo.searchRecipes(user, null, PageRequest.of(0, 2));

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("userId"), eq(2L));
        verify(query, never())
                .setParameter(eq("query"), any());
        verify(query, never())
                .setParameter(eq("ownerIds"), any());
    }

    @Test
    public void noOwnerWithFilter() {
        String filter = "a neat filter";
        String tsquery = "parsed to query";
        doReturn(tsquery)
                .when(queryConverter)
                .convert(filter);

        repo.searchRecipes(user, filter, PageRequest.of(0, 2));

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("userId"), eq(2L));
        verify(query)
                .setParameter(eq("query"), queryCaptor.capture());
        assertEquals(tsquery, queryCaptor.getValue());
        verify(query, never())
                .setParameter(eq("ownerIds"), any());
    }

    @Test
    public void ownerNoFilter() {
        repo.searchRecipesByOwner(user, List.of(user), "", PageRequest.of(0, 2));

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("userId"), eq(2L));
        verify(query, never())
                .setParameter(eq("query"), any());
        verify(query)
                .setParameter(eq("ownerIds"), eq(Set.of(2L)));
    }

    @Test
    public void ownerWithFilter() {
        repo.searchRecipesByOwner(user, List.of(user), "chicken thighs", PageRequest.of(0, 2));

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("userId"), eq(2L));
        verify(query)
                .setParameter(eq("ownerIds"), eq(Set.of(2L)));
        verify(query)
                .setParameter(eq("query"), queryCaptor.capture());
    }

}
