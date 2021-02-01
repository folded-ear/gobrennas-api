package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.IngredientRef;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.IngredientInfo;
import com.brennaswitzer.cookbook.payload.UserInfo;
import com.brennaswitzer.cookbook.repositories.RecipeRepository;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("share/recipe")
public class ShareRecipeController {

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RecipeRepository repo;

    @Autowired
    private RecipeController recipeController; // todo: oof

    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/for/{id}")
    @ResponseBody
    public Object getRecipeShareLink(
            @PathVariable("id") Long id
    ) {
        Recipe r = repo.getOne(id);
        String secret = getSecretForId(id);
        String slug = r.getName()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        if (slug.length() > 35) {
            slug = slug.substring(0, 30);
        }
        slug = slug.trim().replace(' ', '-');

        Map<String, Object> result = new HashMap<>();
        result.put("secret", secret);
        result.put("slug", slug);
        // I know there's a smarter way to do this; linking w/in apps isn't a
        // new problem. But I've never actually built a Spring app that does it.
        result.put("url", "share/recipe/" + slug + "/" + secret + "/" + id + ".json");
        return result;
    }

    @GetMapping("/{slug}/{secret}/{id}.json")
    @ResponseBody
    public Object getSharedRecipe(
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
                .collect(
                        HashMap::new,
                        (m, i) -> m.put(i.getId(), i),
                        HashMap::putAll
                ));
        return result;
    }

    private String getSecretForId(Long id) {
        return HmacUtils.hmacSha1Hex(
                appProperties.getAuth().getTokenSecret().getBytes(),
                id.toString().getBytes()
        );
    }
}
