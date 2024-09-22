package com.brennaswitzer.cookbook.services.favorites;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.repositories.FavoriteRepository;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FetchFavorites {

    @Autowired
    private FavoriteRepository repo;

    @Autowired
    private UserPrincipalAccess principalAccess;

    public List<Favorite> all() {
        return repo.findByOwnerId(principalAccess.getId());
    }

    public List<Favorite> byType(String objectType) {
        return repo.findByOwnerIdAndObjectType(principalAccess.getId(),
                                             objectType);
    }

    public Optional<Favorite> byObject(String objectType, Long objectId) {
        return repo.findByOwnerIdAndObjectTypeAndObjectId(principalAccess.getId(),
                                                        objectType,
                                                        objectId);
    }

    public Iterable<Favorite> byObjects(String objectType, Collection<Long> objectIds) {
        return repo.findByOwnerIdAndObjectTypeAndObjectIdIn(principalAccess.getId(),
                                                          objectType,
                                                          objectIds);
    }

}
