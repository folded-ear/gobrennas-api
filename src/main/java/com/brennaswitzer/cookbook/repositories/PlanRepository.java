package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Plan;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Query;

public interface PlanRepository extends BaseEntityRepository<Plan> {

    @Query("""
           from Plan
           where acl.owner = ?1
           order by name, id
           """)
    Iterable<Plan> findByOwner(User owner);

    @Query("""
           select l
           from Plan l
           where l.acl.owner.id = ?1
               or exists (
                   select 1
                   from l.acl.grants gs
                   where key(gs).id = ?1
               )
           order by l.name, l.id
           """)
    Iterable<Plan> findAccessiblePlans(Long userId);

    @Query("select coalesce(max(position), -1) from Plan where acl.owner = ?1")
    int getMaxPosition(User owner);

}
