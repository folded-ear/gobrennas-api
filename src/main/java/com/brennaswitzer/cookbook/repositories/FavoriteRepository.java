package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Favorite;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends BaseEntityRepository<Favorite> {

    List<Favorite> findByOwnerId(Long ownerId);

    List<Favorite> findByOwnerIdAndObjectType(Long ownerId,
                                              String objectType);

    Optional<Favorite> findByOwnerIdAndObjectTypeAndObjectId(Long ownerId,
                                                             String objectType,
                                                             Long objectId);

    Iterable<Favorite> findByOwnerIdAndObjectTypeAndObjectIdIn(Long ownerId,
                                                               String objectType,
                                                               Collection<Long> objectIds);

    int deleteByOwnerIdAndObjectTypeAndObjectId(Long ownerId,
                                                String objectType,
                                                Long objectId);

}
