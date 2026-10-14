package com.parkeye.pvs.adapter.repository;

import com.parkeye.pvs.adapter.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<ZoneEntity, Long> {
    Optional<ZoneEntity> findByZoneCodeIgnoreCase(String zoneCode);
}
