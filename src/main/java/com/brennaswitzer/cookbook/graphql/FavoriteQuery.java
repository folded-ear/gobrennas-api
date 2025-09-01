package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FavoriteQuery {

    @Autowired
    private FetchFavorites fetchFavorites;

    @SchemaMapping(typeName = "FavoriteQuery")
    @PreAuthorize("hasRole('USER')")
    public List<Favorite> all(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.all(userPrincipal);
    }

    @SchemaMapping(typeName = "FavoriteQuery")
    @PreAuthorize("hasRole('USER')")
    public List<Favorite> byType(@Argument String objectType,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.byType(userPrincipal,
                                     objectType);
    }

    @SchemaMapping(typeName = "FavoriteQuery")
    @PreAuthorize("hasRole('USER')")
    public Favorite byObject(@Argument String objectType,
                             @Argument Long objectId,
                             @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.byObject(userPrincipal,
                                       objectType,
                                       objectId)
                .orElse(null);
    }

}
