package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TextractJobRepository extends BaseEntityRepository<TextractJob> {

    List<TextractJob> findAllByOwnerOrderByCreatedAtDesc(User owner);

    List<TextractJob> findAllByUpdatedAtBefore(Instant cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TextractJob> findLockedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TextractJob> findLockedByIdAndUpdatedAtBefore(Long id,
                                                           Instant cutoff);

}
