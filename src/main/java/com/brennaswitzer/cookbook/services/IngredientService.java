package com.brennaswitzer.cookbook.services;

import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.domain.User;
import com.brennaswitzer.cookbook.repositories.PantryItemRepository;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.EnglishUtils;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    public Iterable<Ingredient> findAllIngredientsByNameContaining(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        List<PantryItem> pantryItems = pantryItemRepository.findAllByNameIgnoreCaseContainingOrderById(unpluralized);
        List<Ingredient> result = new ArrayList<>(pantryItems);
        User user = principalAccess.getUser();
        List<Recipe> recipes = recipeRepository.findAllByOwnerAndNameIgnoreCaseContainingOrderById(user, unpluralized);
        result.addAll(recipes);
        return result;
    }

    public Optional<? extends Ingredient> findIngredientByName(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        // see if there's a pantry item...
        List<PantryItem> pantryItem = pantryItemRepository.findByNameIgnoreCaseOrderById(unpluralized);
        if (!pantryItem.isEmpty()) {
            return pantryItem.stream().findFirst();
        }
        // see if there's a recipe...
        User user = principalAccess.getUser();
        List<Recipe> recipe = recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(user, name);
        if (!recipe.isEmpty() || name.equals(unpluralized)) {
            return recipe.stream().findFirst();
        }
        return recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(user, unpluralized)
                .stream()
                .findFirst();
    }

}
