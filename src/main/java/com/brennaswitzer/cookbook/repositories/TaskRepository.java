package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Iterable<Task> findByParentIsNull();

    @Query("select coalesce(max(position), -1) from Task where parent is null")
    int getMaxRootPosition();

}
