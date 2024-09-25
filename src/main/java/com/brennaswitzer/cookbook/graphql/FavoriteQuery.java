package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class FavoriteQuery {

    @Autowired
    private FetchFavorites fetchFavorites;

    public List<Favorite> all(DataFetchingEnvironment env) {
        return fetchFavorites.all(PrincipalUtil.from(env));
    }

    public List<Favorite> byType(String objectType,
                                 DataFetchingEnvironment env) {
        return fetchFavorites.byType(PrincipalUtil.from(env),
                                     objectType);
    }

    public Favorite byObject(String objectType,
                             Long objectId,
                             DataFetchingEnvironment env) {
        return fetchFavorites.byObject(PrincipalUtil.from(env),
                                       objectType,
                                       objectId)
                .orElse(null);
    }

}
