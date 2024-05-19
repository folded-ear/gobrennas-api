package com.brennaswitzer.cookbook.graphql.loaders;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.FavoriteType;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeIsFavoriteBatchLoaderTest {

    @InjectMocks
    private RecipeIsFavoriteBatchLoader loader;

    @Mock
    private FavoriteRepository repo;

    @Test
    void happyPath() {
        User owner = mock(User.class);
        when(owner.isOwnerOf(any())).thenReturn(true, true, false);
        Recipe one = mockRecipe(1L); // owned, but not fav
        Recipe two = mockRecipe(2L); // owned and fav
        Recipe three = mockRecipe(3L); // not owned
        Favorite favTwo = mock(Favorite.class);
        when(favTwo.getObjectId()).thenReturn(2L);
        when(repo.findByOwnerAndObjectTypeAndObjectIdIn(any(), any(), any()))
                .thenReturn(List.of(favTwo));

        List<Boolean> result = loader.loadInternal(owner,
                                                   List.of(one, two, three));

        assertEquals(List.of(false, true, false), result);
        verify(repo).findByOwnerAndObjectTypeAndObjectIdIn(
                owner,
                FavoriteType.RECIPE.getKey(),
                List.of(1L, 2L));
    }

    @Test
    void noOwnedRecipes() {
        User owner = mock(User.class);
        when(owner.isOwnerOf(any())).thenReturn(false);

        List<Boolean> result = loader.loadInternal(owner,
                                                   List.of(mock(Recipe.class),
                                                           mock(Recipe.class)));

        assertEquals(List.of(false, false), result);
        verifyNoInteractions(repo);
    }

    private static Recipe mockRecipe(long id) {
        Recipe mock = mock(Recipe.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

}
