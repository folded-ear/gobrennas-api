package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Task;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {

    Iterable<Task> findByParentIsNull();

}
