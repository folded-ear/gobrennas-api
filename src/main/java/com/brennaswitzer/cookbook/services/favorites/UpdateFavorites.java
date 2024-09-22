package com.brennaswitzer.cookbook.services.favorites;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateFavorites {

    @Autowired
    private FavoriteRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public Favorite ensureFavorite(String objectType, Long objectId) {
        return repo.findByOwnerIdAndObjectTypeAndObjectId(principalAccess.getId(),
                                                        objectType,
                                                        objectId)
                .orElseGet(() -> {
                    Favorite fav = new Favorite();
                    fav.setOwner(principalAccess.getUser());
                    fav.setObjectType(objectType);
                    fav.setObjectId(objectId);
                    return repo.save(fav);
                });
    }

    public boolean ensureNotFavorite(String objectType, Long objectId) {
        return 0 < repo.deleteByOwnerIdAndObjectTypeAndObjectId(principalAccess.getId(),
                                                              objectType,
                                                              objectId);
    }

}
