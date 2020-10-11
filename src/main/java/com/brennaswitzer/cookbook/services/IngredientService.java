package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IngredientService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private PantryItemRepository pantryItemRepository;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Ingredient ensureIngredientByName(String name) {
        Optional<? extends Ingredient> oing = findIngredientByName(name);
        if (oing.isPresent()) {
            return oing.get();
        }
        // make a new pantry item
        return pantryItemRepository.save(new PantryItem(EnglishUtils.unpluralize(name)));
    }

    public Optional<? extends Ingredient> findIngredientByName(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        // see if there's a pantry item...
        List<PantryItem> pantryItem = pantryItemRepository.findByNameIgnoreCaseOrderById(unpluralized);
        if (!pantryItem.isEmpty()) {
            return pantryItem.stream().findFirst();
        }
        // see if there's a recipe...
        List<Recipe> recipe = recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(principalAccess.getUser(), name);
        if (!recipe.isEmpty()) {
            return recipe.stream().findFirst();
        }
        return recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(principalAccess.getUser(), unpluralized)
                .stream()
                .findFirst();
    }

}
