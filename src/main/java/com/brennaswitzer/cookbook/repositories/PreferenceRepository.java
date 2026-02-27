package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Preference;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferenceRepository extends BaseEntityRepository<Preference> {

    Optional<Preference> findByName(String name);

    Preference getByName(String name);

}
