package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Favorite;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends BaseEntityRepository<Favorite> {

    List<Favorite> findByOwner(User owner);

    List<Favorite> findByOwnerAndObjectType(User owner,
                                            String objectType);

    Optional<Favorite> findByOwnerAndObjectTypeAndObjectId(User owner,
                                                           String objectType,
                                                           Long objectId);

    Iterable<Favorite> findByOwnerAndObjectTypeAndObjectIdIn(User owner,
                                                             String objectType,
                                                             Collection<Long> objectIds);

    int deleteByOwnerAndObjectTypeAndObjectId(User owner,
                                              String objectType,
                                              Long objectId);

}
