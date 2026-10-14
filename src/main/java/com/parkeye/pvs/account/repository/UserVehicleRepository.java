package com.parkeye.pvs.account.repository;

import com.parkeye.pvs.account.entity.UserVehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVehicleRepository extends JpaRepository<UserVehicleEntity, Long> {
    List<UserVehicleEntity> findByUserIdOrderByIdAsc(Long userId);
    Optional<UserVehicleEntity> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndLicensePlateIgnoreCase(Long userId, String licensePlate);
}
