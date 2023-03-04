package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.services.favorites.UpdateFavorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@SuppressWarnings("unused")
@Component
public class FavoriteMutation {

    @Autowired
    private UpdateFavorites updateFavorites;

    Favorite markFavorite(String objectType, Long objectId) {
        return updateFavorites.ensureFavorite(objectType,
                                              objectId);
    }

    boolean removeFavorite(String objectType, Long objectId) {
        return updateFavorites.ensureNotFavorite(objectType,
                                                 objectId);
    }

}
