package com.brennaswitzer.cookbook.repositories;

import com.brennaswitzer.cookbook.domain.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {

    Optional<AppSetting> findByName(String name);

}
