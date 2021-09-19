package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
interface BaseEntityRepository<T extends BaseEntity> extends JpaRepository<T, Long> {}
