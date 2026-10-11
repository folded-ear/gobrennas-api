package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TextractJobRepository extends BaseEntityRepository<TextractJob> {

    List<TextractJob> findAllByOwnerOrderByCreatedAtDesc(User owner);

    /**
     * I make the job pending again, if it is a retryable error, and return
     * how many jobs that was.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
           update versioned TextractJob
           set status = PENDING,
               errorMessage = null,
               retryable = false
           where id = ?1
             and status = ERROR
             and retryable = true""")
    int retry(Long id);

}
