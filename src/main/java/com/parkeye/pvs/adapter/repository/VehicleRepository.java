package com.parkeye.pvs.adapter.repository;

import com.parkeye.pvs.adapter.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {}