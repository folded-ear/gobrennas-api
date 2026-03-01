package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface UserRepository extends BaseEntityRepository<User> {
    Optional<User> findByEmail(String email);

    User getByName(String name);

    // kludge for ProfileQuery's pretend implementation
    List<User> findByIdNot(Long idToExclude);

    @Query("""
           SELECT u
             FROM User u
            WHERE NOT EXISTS (
                      FROM Plan p
                     WHERE p.acl.owner = u)
           """)
    Stream<User> findUsersWithoutAPlan();

}
