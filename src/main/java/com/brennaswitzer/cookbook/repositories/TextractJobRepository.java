package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TextractJobRepository extends JpaRepository<TextractJob, Long> {

    List<TextractJob> findAllByOwnerOrderByCreatedAtDesc(User owner);

}
