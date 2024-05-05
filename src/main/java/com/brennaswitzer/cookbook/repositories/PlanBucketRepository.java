package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlanBucket;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.stream.Stream;

public interface PlanBucketRepository extends BaseEntityRepository<PlanBucket>, RevisionRepository<PlanBucket, Long, Long> {

    Stream<PlanBucket> streamAllByPlanIdAndDateIsNotNull(Long id);

}
