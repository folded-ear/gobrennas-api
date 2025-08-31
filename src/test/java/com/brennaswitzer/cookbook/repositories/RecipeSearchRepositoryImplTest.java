package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchRequest;
import com.brennaswitzer.cookbook.repositories.impl.LibrarySearchScope;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.hibernate.query.SynchronizeableQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
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
        // only used when no query is provided.
        lenient().doReturn(2L)
                .when(user)
                .getId();
    }

    @Test
    public void pagingShenanigans() {
        doReturn(List.of(new Recipe(), new Recipe(), new Recipe()))
                .when(query)
                .getResultList();

        SearchResponse<Recipe> result = repo.searchRecipes(LibrarySearchRequest.builder()
                                                                   .user(user)
                                                                   .limit(2)
                                                                   .offset(6)
                                                                   .build());

        verify(query)
                .setFirstResult(6);
        verify(query)
                .setMaxResults(2 + 1);
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertEquals(2, result.size());
    }

    @Test
    public void noOwnerNoFilter() {
        repo.searchRecipes(LibrarySearchRequest.builder()
                                   .user(user)
                                   .scope(LibrarySearchScope.EVERYONE)
                                   .build());

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

        repo.searchRecipes(LibrarySearchRequest.builder()
                                   .user(user)
                                   .scope(LibrarySearchScope.EVERYONE)
                                   .filter(filter)
                                   .limit(2)
                                   .build());

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("query"), queryCaptor.capture());
        assertEquals(tsquery, queryCaptor.getValue());
        verify(query, never())
                .setParameter(eq("ownerIds"), any());
    }

    @Test
    public void ownerNoFilter() {
        repo.searchRecipes(LibrarySearchRequest.builder()
                                   .user(user)
                                   .scope(LibrarySearchScope.MINE)
                                   .filter("")
                                   .limit(2)
                                   .build());

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
        repo.searchRecipes(LibrarySearchRequest.builder()
                                   .user(user)
                                   .scope(LibrarySearchScope.MINE)
                                   .filter("chicken thighs")
                                   .limit(2)
                                   .build());

        verify(entityManager)
                .createNativeQuery(sqlCaptor.capture(), eq(Recipe.class));
        verify(query)
                .setParameter(eq("ownerIds"), eq(Set.of(2L)));
        verify(query)
                .setParameter(eq("query"), queryCaptor.capture());
    }

}
