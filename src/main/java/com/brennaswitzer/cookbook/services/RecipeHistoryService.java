package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.AccessLevel;
import com.brennaswitzer.cookbook.domain.PlannedRecipeHistory;
import com.brennaswitzer.cookbook.domain.Rating;
import com.brennaswitzer.cookbook.repositories.PlannedRecipeHistoryRepository;
import com.brennaswitzer.cookbook.security.permission.PlannedRecipeHistoryAccess;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class RecipeHistoryService {

    @Autowired
    private PlannedRecipeHistoryRepository repo;

    @PlannedRecipeHistoryAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlannedRecipeHistory setRating(Long recipeId, Long id, Rating rating) {
        var h = getHistoryItem(recipeId, id);
        h.setRating(rating);
        return h;
    }

    @PlannedRecipeHistoryAccess(id = "#id", level = AccessLevel.CHANGE)
    public PlannedRecipeHistory setNotes(Long recipeId, Long id, String notes) {
        var h = getHistoryItem(recipeId, id);
        h.setNotes(notes);
        return h;
    }

    private PlannedRecipeHistory getHistoryItem(Long recipeId, Long id) {
        var h = repo.getReferenceById(id);
        if (!Objects.equals(recipeId, h.getRecipe().getId())) {
            throw new EntityNotFoundException("No history %s:%s found".formatted(recipeId, id));
        }
        return h;
    }

}
