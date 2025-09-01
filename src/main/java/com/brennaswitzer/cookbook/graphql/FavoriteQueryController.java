package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FavoriteQueryController {

    record FavoriteQuery() {}

    @Autowired
    private FetchFavorites fetchFavorites;

    @QueryMapping
    FavoriteQuery favorite() {
        return new FavoriteQuery();
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    List<Favorite> all(FavoriteQuery favQ,
                       @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.all(userPrincipal);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    List<Favorite> byType(FavoriteQuery favQ,
                          @Argument String objectType,
                          @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.byType(userPrincipal,
                                     objectType);
    }

    @SchemaMapping
    @PreAuthorize("hasRole('USER')")
    Favorite byObject(FavoriteQuery favQ,
                      @Argument String objectType,
                      @Argument Long objectId,
                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return fetchFavorites.byObject(userPrincipal,
                                       objectType,
                                       objectId)
                .orElse(null);
    }

}
