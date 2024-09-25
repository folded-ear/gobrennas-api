package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.favorites.UpdateFavorites;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class FavoriteMutation {

    @Autowired
    private UpdateFavorites updateFavorites;

    public Favorite markFavorite(String objectType,
                                 Long objectId,
                                 DataFetchingEnvironment env) {
        return updateFavorites.ensureFavorite(PrincipalUtil.from(env),
                                              objectType,
                                              objectId);
    }

    public boolean removeFavorite(String objectType,
                                  Long objectId,
                                  DataFetchingEnvironment env) {
        return updateFavorites.ensureNotFavorite(PrincipalUtil.from(env),
                                                 objectType,
                                                 objectId);
    }

}
