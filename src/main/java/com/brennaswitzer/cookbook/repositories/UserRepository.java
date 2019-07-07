package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    User getByName(String name);

    Boolean existsByEmail(String email);

    User getById(Long id);

    // kludge for FriendController's pretend implementation
    List<User> findByIdNot(Long idToExclude);

}
