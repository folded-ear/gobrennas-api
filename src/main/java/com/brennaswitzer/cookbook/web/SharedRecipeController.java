package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.UserInfo;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shared/recipe")
public class SharedRecipeController {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RecipeRepository repo;

    @Autowired
    private RecipeController recipeController; // todo: oof

    @GetMapping("/{slug}/{secret}/{id}.json")
    @ResponseBody
    public Object getSharedRecipe(
            @SuppressWarnings("unused") @PathVariable("slug") String slug,
            @PathVariable("id") Long id,
            @PathVariable("secret") String secret
    ) {
        if (!getSecretForId(id).equals(secret)) {
            throw new AuthorizationServiceException("Bad secret");
        }
        Map<String, Object> result = new HashMap<>();
        Recipe r = repo.getOne(id);
        result.put("recipe", recipeController.getRecipeInfo(r));
        result.put("owner", UserInfo.fromUser(r.getOwner()));
        result.put("ingredients", r.getIngredients()
                .stream()
                .filter(IngredientRef::hasIngredient)
                .map(IngredientRef::getIngredient)
                .map(IngredientInfo::from)
                .collect(Collectors.toList()));
        return result;
    }

    protected String getSecretForId(Long id) {
        return new HmacUtils(
                HmacAlgorithms.HMAC_SHA_1,
                appProperties.getAuth().getTokenSecret().getBytes()
        ).hmacHex(
                id.toString().getBytes()
        );
    }
}
