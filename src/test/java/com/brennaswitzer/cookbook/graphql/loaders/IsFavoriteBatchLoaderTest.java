package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsFavoriteBatchLoaderTest {

    @InjectMocks
    private IsFavoriteBatchLoader loader;

    @Mock
    private FavoriteRepository repo;

    @Test
    void happyPath() {
        User user = mockUser(456L);
        Favorite favTwo = mock(Favorite.class);
        when(favTwo.getOwner()).thenReturn(user);
        when(favTwo.getObjectType()).thenReturn(FavoriteType.RECIPE.getKey());
        when(favTwo.getObjectId()).thenReturn(2L);
        when(repo.findByOwnerIdAndObjectTypeAndObjectIdIn(eq(123L), any(), any()))
                .thenReturn(List.of());
        when(repo.findByOwnerIdAndObjectTypeAndObjectIdIn(eq(456L), any(), any()))
                .thenReturn(List.of(favTwo));

        List<Boolean> result = loader.loadInternal(List.of(
                new FavKey(123L, FavoriteType.RECIPE, 1L),
                new FavKey(456L, FavoriteType.RECIPE, 2L),
                new FavKey(123L, FavoriteType.RECIPE, 3L)));

        assertEquals(List.of(false, true, false), result);
        verify(repo).findByOwnerIdAndObjectTypeAndObjectIdIn(
                123L,
                FavoriteType.RECIPE.getKey(),
                Set.of(1L, 3L));
        verify(repo).findByOwnerIdAndObjectTypeAndObjectIdIn(
                456L,
                FavoriteType.RECIPE.getKey(),
                Set.of(2L));
        verifyNoMoreInteractions(repo);
    }

    private static User mockUser(@SuppressWarnings("SameParameterValue") long id) {
        var mock = mock(User.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

}
