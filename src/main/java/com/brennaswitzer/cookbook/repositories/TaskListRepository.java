package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TaskList;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface TaskListRepository extends BaseEntityRepository<TaskList> {

    @Query("from TaskList where acl.owner = ?1")
    Iterable<TaskList> findByOwner(User owner);

    @Query("select l\n" +
            "from TaskList l\n" +
            "where l.acl.owner.id = ?1\n" +
            "    or exists (\n" +
            "        select 1\n" +
            "        from l.acl.grants gs\n" +
            "        where key(gs) = ?1\n" +
            "    )\n" +
            "order by l.name, l.id")
    Iterable<TaskList> findAccessibleLists(Long userId);

    @Query("select coalesce(max(position), -1) from TaskList where acl.owner = ?1")
    int getMaxPosition(User owner);

}
