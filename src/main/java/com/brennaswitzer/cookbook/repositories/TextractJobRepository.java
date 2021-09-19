package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.TextractJob;
import com.brennaswitzer.cookbook.domain.User;

import java.util.List;

public interface TextractJobRepository extends BaseEntityRepository<TextractJob> {

    List<TextractJob> findAllByOwnerOrderByCreatedAtDesc(User owner);

}
