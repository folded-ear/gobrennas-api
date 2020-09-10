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
        Optional<PantryItem> pantryItem = pantryItemRepository.findOneByNameIgnoreCase(unpluralized);
        if (pantryItem.isPresent()) {
            return pantryItem;
        }
        // see if there's a recipe...
        Optional<Recipe> recipe = recipeRepository.findOneByOwnerAndNameIgnoreCase(principalAccess.getUser(), name);
        if (recipe.isPresent()) {
            return recipe;
        }
        return recipeRepository.findOneByOwnerAndNameIgnoreCase(principalAccess.getUser(), unpluralized);
    }

}
