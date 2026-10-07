package com.parkeye.pvs.adapter.repository;

import com.parkeye.pvs.adapter.entity.ParkingTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicketEntity, Long> {
    List<ParkingTicketEntity> findByLicensePlateIgnoreCaseOrderByEndTimeDesc(String licensePlate);

    @Query("SELECT DISTINCT t.licensePlate FROM ParkingTicketEntity t WHERE UPPER(t.zoneCode) = UPPER(:zoneCode) AND t.isPaid = true AND t.endTime > CURRENT_TIMESTAMP")
    List<String> findActivePlatesByZone(String zoneCode);

    @Query("SELECT COUNT(t) FROM ParkingTicketEntity t WHERE UPPER(t.zoneCode) = UPPER(:zoneCode) AND t.isPaid = true AND t.endTime > CURRENT_TIMESTAMP")
    long countActiveTicketsByZone(String zoneCode);
}