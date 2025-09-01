package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.graphql.support.PrincipalUtil;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FavoriteQuery {

    @Autowired
    private FetchFavorites fetchFavorites;

    @SchemaMapping(typeName = "FavoriteQuery")
    public List<Favorite> all(DataFetchingEnvironment env) {
        return fetchFavorites.all(PrincipalUtil.from(env));
    }

    @SchemaMapping(typeName = "FavoriteQuery")
    public List<Favorite> byType(@Argument String objectType,
                                 DataFetchingEnvironment env) {
        return fetchFavorites.byType(PrincipalUtil.from(env),
                                     objectType);
    }

    @SchemaMapping(typeName = "FavoriteQuery")
    public Favorite byObject(@Argument String objectType,
                             @Argument Long objectId,
                             DataFetchingEnvironment env) {
        return fetchFavorites.byObject(PrincipalUtil.from(env),
                                       objectType,
                                       objectId)
                .orElse(null);
    }

}
