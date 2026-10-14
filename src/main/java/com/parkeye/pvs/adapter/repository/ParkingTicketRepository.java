package com.parkeye.pvs.adapter.repository;

import com.parkeye.pvs.adapter.entity.ParkingTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ParkingTicketRepository extends JpaRepository<ParkingTicketEntity, Long> {
    List<ParkingTicketEntity> findByLicensePlateIgnoreCaseOrderByEndTimeDesc(String licensePlate);

    @Query("SELECT DISTINCT t.licensePlate FROM ParkingTicketEntity t WHERE UPPER(t.zoneCode) = UPPER(:zoneCode) AND t.isPaid = true AND t.endTime > CURRENT_TIMESTAMP")
    List<String> findActivePlatesByZone(@Param("zoneCode") String zoneCode);

    @Query("SELECT COUNT(t) FROM ParkingTicketEntity t WHERE UPPER(t.zoneCode) = UPPER(:zoneCode) AND t.isPaid = true AND t.endTime > CURRENT_TIMESTAMP")
    long countActiveTicketsByZone(@Param("zoneCode") String zoneCode);

    /** One query for the occupancy of every zone (avoids N+1 when listing zones). */
    @Query("SELECT new com.parkeye.pvs.adapter.repository.ZoneOccupancy(UPPER(t.zoneCode), COUNT(t)) FROM ParkingTicketEntity t WHERE t.isPaid = true AND t.endTime > CURRENT_TIMESTAMP GROUP BY UPPER(t.zoneCode)")
    List<ZoneOccupancy> countActiveTicketsPerZone();

    /** {@code plates} must already be upper-cased. */
    @Query("SELECT t FROM ParkingTicketEntity t WHERE UPPER(t.licensePlate) IN :plates ORDER BY t.startTime DESC")
    List<ParkingTicketEntity> findByPlates(@Param("plates") Collection<String> plates);
}
