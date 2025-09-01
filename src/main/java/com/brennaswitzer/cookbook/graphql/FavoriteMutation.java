package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import com.brennaswitzer.cookbook.services.favorites.UpdateFavorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class FavoriteMutation {

    @Autowired
    private UpdateFavorites updateFavorites;

    @SchemaMapping(typeName = "FavoriteMutation")
    @PreAuthorize("hasRole('USER')")
    public Favorite markFavorite(@Argument String objectType,
                                 @Argument Long objectId,
                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return updateFavorites.ensureFavorite(userPrincipal,
                                              objectType,
                                              objectId);
    }

    @SchemaMapping(typeName = "FavoriteMutation")
    @PreAuthorize("hasRole('USER')")
    public boolean removeFavorite(@Argument String objectType,
                                  @Argument Long objectId,
                                  @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return updateFavorites.ensureNotFavorite(userPrincipal,
                                                 objectType,
                                                 objectId);
    }

}
