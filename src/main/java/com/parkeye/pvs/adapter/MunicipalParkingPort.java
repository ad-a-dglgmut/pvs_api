package com.parkeye.pvs.adapter;

import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.Vehicle;
import java.util.List;

public interface MunicipalParkingPort {
    List<ParkingZone> fetchAllZones();
    List<Vehicle> fetchAllVehicles();
    ScoutVerificationResponse verifyParking(String licensePlate, String zoneCode);
    List<ScanHistoryDto> getRecentScans();
    DashboardStatsDto getTodayStats();
    List<String> getActiveVehiclesInZone(String zoneCode);
    void cancelLastScan(String licensePlate);
    void clearAllScans();
}