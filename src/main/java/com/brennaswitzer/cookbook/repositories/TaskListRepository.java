package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    @Query("from TaskList where acl.owner = ?1")
    Iterable<TaskList> findByOwner(User owner);

    @Query("from TaskList where acl.owner.id = ?1")
    Iterable<TaskList> findByOwnerId(Long ownerId);

    @Query("select coalesce(max(position), -1) from TaskList where acl.owner = ?1")
    int getMaxPosition(User owner);

}
