package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.S3File;
import com.brennaswitzer.cookbook.domain.Upload;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
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

        Recipe result = service.createNewRecipe(input, photo);

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

        Recipe result = service.createNewRecipe(input);

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

        Recipe result = service.updateRecipe(saved, photo);

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

        Recipe result = service.updateRecipe(saved, photo);

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

        Recipe result = service.updateRecipe(saved);

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

        Recipe result = service.updateRecipe(input);

        assertSame(saved, result);
        verifyNoInteractions(storageService);
    }

}
