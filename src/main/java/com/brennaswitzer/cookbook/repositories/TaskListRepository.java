package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface TaskListRepository extends BaseEntityRepository<Plan> {

    @Query("from Plan where acl.owner = ?1")
    Iterable<Plan> findByOwner(User owner);

    @Query("select l\n" +
            "from Plan l\n" +
            "where l.acl.owner.id = ?1\n" +
            "    or exists (\n" +
            "        select 1\n" +
            "        from l.acl.grants gs\n" +
            "        where key(gs) = ?1\n" +
            "    )\n" +
            "order by l.name, l.id")
    Iterable<Plan> findAccessibleLists(Long userId);

    @Query("select coalesce(max(position), -1) from Plan where acl.owner = ?1")
    int getMaxPosition(User owner);

}
