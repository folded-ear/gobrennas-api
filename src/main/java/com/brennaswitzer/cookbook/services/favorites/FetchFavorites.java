package com.brennaswitzer.cookbook.services.favorites;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FetchFavorites {

    @Autowired
    private FavoriteRepository repo;

    public List<Favorite> all(UserPrincipal principal) {
        return repo.findByOwnerId(principal.getId());
    }

    public List<Favorite> byType(UserPrincipal principal,
                                 String objectType) {
        return repo.findByOwnerIdAndObjectType(
                principal.getId(),
                objectType);
    }

    public Optional<Favorite> byObject(UserPrincipal principal,
                                       String objectType,
                                       Long objectId) {
        return repo.findByOwnerIdAndObjectTypeAndObjectId(
                principal.getId(),
                objectType,
                objectId);
    }

}
