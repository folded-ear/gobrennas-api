package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.PlanBucket;

import java.util.stream.Stream;

public interface PlanBucketRepository extends BaseEntityRepository<PlanBucket> {

    Stream<PlanBucket> streamAllByPlanIdAndDateIsNotNull(Long id);

}
