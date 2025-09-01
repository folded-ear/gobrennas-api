package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.repositories.impl.SearchResponseImpl;
import com.brennaswitzer.cookbook.services.storage.StorageService;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @InjectMocks
    private RecipeService service;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserPrincipalAccess principalAccess;

    @Mock
    private StorageService storageService;

    @Test
    void createNewRecipe_withPhoto() {
        Recipe input = new Recipe();
        Recipe saved = new Recipe();
        long recipeId = 1234L;
        saved.setId(recipeId);
        Upload photo = mock(Upload.class);
        String filename = "photo.jpg";
        when(photo.getOriginalFilename())
                .thenReturn(filename);
        when(recipeRepository.save(input))
                .thenReturn(saved);

        Recipe result = service.createNewRecipe(input, null, photo);

        assertSame(saved, result);
        verify(storageService)
                .store(photo,
                       String.format("recipe/%d/%s",
                                     recipeId,
                                     filename));
    }

    @Test
    void createRecipe_noPhoto() {
        Recipe input = new Recipe();
        Recipe saved = new Recipe();
        when(recipeRepository.save(input))
                .thenReturn(saved);

        Recipe result = service.createNewRecipe(input, null, null);

        assertSame(saved, result);
        verifyNoInteractions(storageService);
    }

    @Test
    void updateRecipe_addPhoto() {
        User owner = new User();
        long recipeId = 123L;
        Upload photo = mock(Upload.class);
        String filename = "photo.jpg";
        when(photo.getOriginalFilename())
                .thenReturn(filename);
        Recipe saved = new Recipe();
        saved.setId(recipeId);
        saved.setOwner(owner);
        when(recipeRepository.getReferenceById(recipeId))
                .thenReturn(saved);
        when(recipeRepository.save(saved))
                .thenReturn(saved);
        when(principalAccess.getUser())
                .thenReturn(owner);

        Recipe result = service.updateRecipe(saved, null, photo);

        assertSame(saved, result);
        verify(storageService)
                .store(photo,
                       String.format("recipe/%d/%s",
                                     recipeId,
                                     filename));
    }

    @Test
    void updateRecipe_replacePhoto() {
        User owner = new User();
        Upload photo = mock(Upload.class);
        String filename = "photo.jpg";
        when(photo.getOriginalFilename())
                .thenReturn(filename);
        long recipeId = 123L;
        Recipe saved = new Recipe();
        saved.setId(recipeId);
        saved.setOwner(owner);
        String oldObjectKey = "path/to/old_photo.jpg";
        saved.setPhoto(new S3File(oldObjectKey,
                                  "image/jpg",
                                  1234L));
        when(recipeRepository.getReferenceById(recipeId))
                .thenReturn(saved);
        when(recipeRepository.save(saved))
                .thenReturn(saved);
        when(principalAccess.getUser())
                .thenReturn(owner);

        Recipe result = service.updateRecipe(saved, null, photo);

        assertSame(saved, result);
        verify(storageService)
                .remove(oldObjectKey);
        verify(storageService)
                .store(photo,
                       String.format("recipe/%d/%s",
                                     recipeId,
                                     filename));
    }

    @Test
    void updateRecipe_leaveExistingPhoto() {
        User owner = new User();
        long recipeId = 123L;
        Recipe saved = new Recipe();
        saved.setOwner(owner);
        saved.setId(recipeId);
        saved.setPhoto(new S3File("path/to/photo.jpg",
                                  "image/jpg",
                                  1234L));
        when(recipeRepository.getReferenceById(recipeId))
                .thenReturn(saved);
        when(recipeRepository.save(saved))
                .thenReturn(saved);
        when(principalAccess.getUser())
                .thenReturn(owner);

        Recipe result = service.updateRecipe(saved, null, null);

        assertSame(saved, result);
        verifyNoInteractions(storageService);
    }

    @Test
    void updateRecipe_stillNoPhoto() {
        User owner = new User();
        long recipeId = 123L;
        Recipe input = new Recipe();
        input.setId(recipeId);
        Recipe saved = new Recipe();
        saved.setOwner(owner);
        when(recipeRepository.getReferenceById(recipeId))
                .thenReturn(saved);
        when(recipeRepository.save(input))
                .thenReturn(saved);
        when(principalAccess.getUser())
                .thenReturn(owner);

        Recipe result = service.updateRecipe(input, null, null);

        assertSame(saved, result);
        verifyNoInteractions(storageService);
    }

    @Test
    void addOwnedSection() {
        Recipe pizza = new Recipe();
        Recipe crust = new Recipe();

        pizza.addOwnedSection(crust);

        assertEquals(pizza, crust.getSectionOf());
        assertEquals(List.of(crust), pizza.getOwnedSections());
        Iterator<IngredientRef> itr = pizza.getIngredients().iterator();
        IngredientRef ref = itr.next();
        assertFalse(itr.hasNext(), "expected only one ingredient");
        assertTrue(ref.isSection());
        assertEquals(crust, ref.getIngredient());
    }

    @Test
    void removeOwnedSection_orphan() {
        Recipe pizza = new Recipe();
        Recipe crust = new Recipe();
        crust.setId(123L);
        pizza.addOwnedSection(crust);
        when(recipeRepository.searchRecipes(any()))
                .thenReturn(SearchResponseImpl.<Recipe>builder()
                                    .content(List.of(pizza))
                                    .build());

        service.removeOwnedSection(crust);

        assertEquals(List.of(),
                     pizza.getOwnedSections());
        assertEquals(List.of(),
                     pizza.getIngredients());
        verify(recipeRepository).delete(crust);
    }

    @Test
    void removeOwnedSection_rehome() {
        Recipe pizza = new Recipe();
        Recipe pie = new Recipe();
        Recipe crust = new Recipe();
        crust.setId(123L);
        pizza.addOwnedSection(crust);
        pie.addIngredient(crust) // by reference section
                .setSection(true);
        when(recipeRepository.searchRecipes(any()))
                .thenReturn(SearchResponseImpl.<Recipe>builder()
                                    .content(List.of(pizza, pie))
                                    .build());

        service.removeOwnedSection(crust);

        assertEquals(List.of(),
                     pizza.getOwnedSections());
        assertEquals(List.of(),
                     pizza.getIngredients());
        assertEquals(List.of(crust),
                     pie.getOwnedSections()); // became an owned section
        verify(recipeRepository, never()).delete(any());
    }

}
