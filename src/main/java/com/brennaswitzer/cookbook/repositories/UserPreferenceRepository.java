package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.UserPreference;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends BaseEntityRepository<UserPreference> {
}
