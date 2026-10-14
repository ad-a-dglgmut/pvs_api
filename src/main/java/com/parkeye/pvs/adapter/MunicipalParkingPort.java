package com.parkeye.pvs.adapter;

import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.model.NewParkingSession;
import com.parkeye.pvs.model.ParkingSession;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.Vehicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Everything ParkEye needs from the municipal parking system. The local H2 adapter implements
 * it today; a REST/other-database adapter can replace it without touching services/controllers.
 *
 * Data owned by ParkEye itself (users, wallet, saved vehicles, notifications) is NOT behind this
 * port - it lives in ParkEye's own database.
 */
public interface MunicipalParkingPort {

    // ---- Enforcement (scouts) ----
    List<ParkingZone> fetchAllZones();
    List<Vehicle> fetchAllVehicles();
    ScoutVerificationResponse verifyParking(String licensePlate, String zoneCode);
    List<ScanHistoryDto> getRecentScans();
    DashboardStatsDto getTodayStats();
    List<String> getActiveVehiclesInZone(String zoneCode);
    void cancelLastScan(String licensePlate);
    void clearAllScans();

    // ---- Consumer: zones & sessions ----
    Optional<ParkingZone> findZone(String zoneCode);

    ParkingSession createSession(NewParkingSession session);

    Optional<ParkingSession> findSession(Long sessionId);

    /** All sessions (active and past) for the given plates, newest start first. */
    List<ParkingSession> findSessionsByPlates(Collection<String> licensePlates);

    /** Used for both "extend" (later end, higher cost) and "stop early" (end = now, lower cost, refund). */
    ParkingSession updateSession(Long sessionId, Instant endTime, BigDecimal cost, BigDecimal refunded);
}
