package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.domain.*;
import com.brennaswitzer.cookbook.mapper.IngredientMapper;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.UserInfo;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import com.brennaswitzer.cookbook.util.ShareHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

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
            @SuppressWarnings("unused") @PathVariable("slug") String slug,
            @PathVariable("id") Long id,
            @PathVariable("secret") String secret
    ) {
        if (!helper.isSecretValid(Recipe.class, id, secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        Map<String, Object> result = new HashMap<>();
        Recipe r = repo.getOne(id);
        Queue<IngredientRef> queue = new LinkedList<>(r.getIngredients());
        List<IngredientInfo> ings = new ArrayList<>();
        ings.add(ingredientMapper.recipeToInfo(r));
        while (!queue.isEmpty()) {
            IngredientRef ir = queue.remove();
            if (!ir.hasIngredient()) continue;
            Ingredient i = ir.getIngredient();
            if (i instanceof Recipe) {
                ings.add(ingredientMapper.recipeToInfo((Recipe) i));
            } else if (i instanceof PantryItem) {
                ings.add(ingredientMapper.pantryItemToInfo((PantryItem) i));
            } else {
                ings.add(ingredientMapper.ingredientToInfo(i));
            }
            if (i instanceof AggregateIngredient) {
                queue.addAll(((AggregateIngredient) i).getIngredients());
            }
        }
        result.put("ingredients", ings);
        result.put("owner", UserInfo.fromUser(r.getOwner()));
        return result;
    }

}
