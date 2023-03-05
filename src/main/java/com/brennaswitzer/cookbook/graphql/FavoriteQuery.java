package com.brennaswitzer.cookbook.graphql;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.services.favorites.FetchFavorites;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@SuppressWarnings("unused")
@Component
public class FavoriteQuery {

    @Autowired
    private FetchFavorites fetchFavorites;

    public List<Favorite> all() {
        return fetchFavorites.all();
    }

    public List<Favorite> byType(String objectType) {
        return fetchFavorites.byType(objectType);
    }

    public Favorite byObject(String objectType, Long objectId) {
        return fetchFavorites.byObject(objectType, objectId);
    }

}
