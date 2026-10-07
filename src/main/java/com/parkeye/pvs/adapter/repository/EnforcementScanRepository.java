package com.parkeye.pvs.adapter.repository;

import com.parkeye.pvs.adapter.entity.EnforcementScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnforcementScanRepository extends JpaRepository<EnforcementScanEntity, Long> {
    List<EnforcementScanEntity> findTop15ByOrderByScanTimeDesc();

    long countByScanTimeBetween(LocalDateTime start, LocalDateTime end);
    long countByScanTimeBetweenAndStatus(LocalDateTime start, LocalDateTime end, com.parkeye.pvs.model.ParkingStatus status);
    Optional<EnforcementScanEntity> findTopByLicensePlateOrderByScanTimeDesc(String licensePlate);
}