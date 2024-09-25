package com.brennaswitzer.cookbook.services.favorites;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.repositories.UserRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateFavorites {

    @Autowired
    private FavoriteRepository repo;

    @Autowired
    private UserRepository userRepo;

    public Favorite ensureFavorite(UserPrincipal principal,
                                   String objectType,
                                   Long objectId) {
        return repo.findByOwnerIdAndObjectTypeAndObjectId(
                        principal.getId(),
                        objectType,
                        objectId)
                .orElseGet(() -> {
                    Favorite fav = new Favorite();
                    fav.setOwner(userRepo.getReferenceById(principal.getId()));
                    fav.setObjectType(objectType);
                    fav.setObjectId(objectId);
                    return repo.save(fav);
                });
    }

    public boolean ensureNotFavorite(UserPrincipal principal,
                                     String objectType,
                                     Long objectId) {
        return 0 < repo.deleteByOwnerIdAndObjectTypeAndObjectId(
                principal.getId(),
                objectType,
                objectId);
    }

}
