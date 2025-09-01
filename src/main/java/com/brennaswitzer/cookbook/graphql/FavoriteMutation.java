package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.favorites.UpdateFavorites;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class FavoriteMutation {

    @Autowired
    private UpdateFavorites updateFavorites;

    @SchemaMapping(typeName = "FavoriteMutation")
    public Favorite markFavorite(@Argument String objectType,
                                 @Argument Long objectId,
                                 DataFetchingEnvironment env) {
        return updateFavorites.ensureFavorite(PrincipalUtil.from(env),
                                              objectType,
                                              objectId);
    }

    @SchemaMapping(typeName = "FavoriteMutation")
    public boolean removeFavorite(@Argument String objectType,
                                  @Argument Long objectId,
                                  DataFetchingEnvironment env) {
        return updateFavorites.ensureNotFavorite(PrincipalUtil.from(env),
                                                 objectType,
                                                 objectId);
    }

}
