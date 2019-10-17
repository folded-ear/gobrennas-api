package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.Label;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LabelRepository extends CrudRepository<Label, Long> {

    Optional<Label> findOneByNameIgnoreCase(String label);
}
