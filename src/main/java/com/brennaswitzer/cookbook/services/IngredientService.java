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
import java.util.Collection;
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
        return findIngredientByName(name)
                // otherwise make a new pantry item
                .orElseGet(() -> pantryItemRepository.save(
                        new PantryItem(
                                EnglishUtils.unpluralize(name))));
    }

    public List<Ingredient> findAllIngredientsByNameContaining(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        List<PantryItem> pantryItems = pantryItemRepository.findAllByNameIgnoreCaseContainingOrderById(unpluralized);
        List<Ingredient> result = new ArrayList<>(pantryItems);
        User user = principalAccess.getUser();
        List<Recipe> recipes = recipeRepository.findAllByOwnerAndNameIgnoreCaseContainingAndSectionOfIsNullOrderById(
                user,
                unpluralized);
        result.addAll(recipes);
        return result;
    }

    public Optional<Ingredient> findIngredientByName(String name) {
        String unpluralized = EnglishUtils.unpluralize(name);
        // see if there's a pantry item...
        List<PantryItem> pantryItem = pantryItemRepository.findByNameIgnoreCaseOrderById(unpluralized);
        if (!pantryItem.isEmpty()) {
            return pantryItem.stream()
                    .findFirst()
                    .map(Ingredient.class::cast);
        }
        // see if there's a recipe...
        User user = principalAccess.getUser();
        List<Recipe> recipe = recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(user, name);
        if (!recipe.isEmpty() || name.equals(unpluralized)) {
            return recipe.stream()
                    .findFirst()
                    .map(Ingredient.class::cast);
        }
        return recipeRepository.findByOwnerAndNameIgnoreCaseOrderById(user, unpluralized)
                .stream()
                .findFirst()
                .map(Ingredient.class::cast);
    }

    /**
     * I find a list of all pantry items and recipes that are the union of any
     * records that fuzzy match the name
     *
     * @param names List<String>
     * @return Iterable<Ingredient>
     */
    public Iterable<Ingredient> findAllIngredientsByNamesContaining(List<String> names) {
        List<Ingredient> results = new ArrayList<>();
        for (String name : names) {
            results.addAll(findAllIngredientsByNameContaining(name));
        }
        return results;
    }

    public Collection<Ingredient> bulkIngredients(Collection<Long> ids) {
        Collection<Ingredient> result = new ArrayList<>(ids.size());
        result.addAll(pantryItemRepository.findAllById(ids));
        result.addAll(recipeRepository.findAllById(ids));
        return result;
    }

}
