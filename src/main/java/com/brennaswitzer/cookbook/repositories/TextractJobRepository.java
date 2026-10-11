package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    /**
     * I lock and return the job, if it exists.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select j
           from TextractJob j
           where j.id = ?1""")
    Optional<TextractJob> claim(Long id);

    /**
     * I lock and return the job, if it exists and was last updated before
     * the cutoff.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select j
           from TextractJob j
           where j.id = ?1
             and j.updatedAt < ?2""")
    Optional<TextractJob> claimIfStale(Long id, Instant cutoff);

}
