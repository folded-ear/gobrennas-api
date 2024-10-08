package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.AggregateIngredient;
import com.brennaswitzer.cookbook.domain.Ingredient;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.PantryItem;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.mapper.IngredientMapper;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.UserInfo;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.ShareHelper;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

@Controller
@RequestMapping("/shared/recipe")
public class SharedRecipeController {

    @Autowired
    private ShareHelper helper;

    @Autowired
    private RecipeRepository repo;

    @Autowired
    private IngredientMapper ingredientMapper;

    @GetMapping("/{slug}/{secret}/{id}.json")
    @ResponseBody
    public Object getSharedRecipe(
            @PathVariable("secret") String secret,
            @PathVariable("id") Long id
    ) {
        if (!helper.isSecretValid(Recipe.class, id, secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        Map<String, Object> result = new HashMap<>();
        Recipe recipe = repo.getReferenceById(id);
        Queue<IngredientRef> queue = new LinkedList<>(recipe.getIngredients());
        List<IngredientInfo> ings = new ArrayList<>();
        ings.add(ingredientMapper.recipeToInfo(recipe));
        while (!queue.isEmpty()) {
            IngredientRef ir = queue.remove();
            if (!ir.hasIngredient()) continue;
            var ing = Hibernate.unproxy(ir.getIngredient(), Ingredient.class);
            if (ing instanceof Recipe r) {
                ings.add(ingredientMapper.recipeToInfo(r));
            } else if (ing instanceof PantryItem pi) {
                ings.add(ingredientMapper.pantryItemToInfo(pi));
            } else {
                ings.add(ingredientMapper.ingredientToInfo((Ingredient) ing));
            }
            if (ing instanceof AggregateIngredient agg) {
                queue.addAll(agg.getIngredients());
            }
        }
        result.put("ingredients", ings);
        result.put("owner", UserInfo.fromUser(recipe.getOwner()));
        return result;
    }

}
