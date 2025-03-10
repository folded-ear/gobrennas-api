package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import jakarta.persistence.NoResultException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class RecipeHistoryService {

    @Autowired
    private PlannedRecipeHistoryRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public PlannedRecipeHistory setRating(Long recipeId, Long id, Rating rating) {
        var h = getMyHistoryItem(recipeId, id);
        h.setRating(rating);
        return h;
    }

    public PlannedRecipeHistory setNotes(Long recipeId, Long id, String notes) {
        var h = getMyHistoryItem(recipeId, id);
        h.setNotes(notes);
        return h;
    }

    private @NotNull PlannedRecipeHistory getMyHistoryItem(Long recipeId, Long id) {
        var h = repo.getReferenceById(id);
        if (!Objects.equals(recipeId, h.getRecipe().getId())) {
            throw new NoResultException("No history %s:%s found".formatted(recipeId, id));
        }
        if (!principalAccess.getId().equals(h.getOwner().getId())) {
            throw new AccessDeniedException("You don't have permission to update this history");
        }
        return h;
    }

}
