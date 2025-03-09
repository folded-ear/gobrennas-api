package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Preference;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferenceRepository extends BaseEntityRepository<Preference> {

    Preference getByName(String name);

}
