package com.parkeye.pvs.service;

import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationRequest;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.Vehicle;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingEnforcementService {

    private final MunicipalParkingPort parkingPort;

    public ParkingEnforcementService(MunicipalParkingPort parkingPort) {
        this.parkingPort = parkingPort;
    }

    public List<ParkingZone> getZones() {
        return parkingPort.fetchAllZones();
    }

    public List<Vehicle> getVehicles() {
        return parkingPort.fetchAllVehicles();
    }

    public ScoutVerificationResponse verifyParking(ScoutVerificationRequest request) {
        return parkingPort.verifyParking(request.licensePlate(), request.zoneCode());
    }
    public List<ScanHistoryDto> getRecentScans() {
        return parkingPort.getRecentScans();
    }

    public DashboardStatsDto getTodayStats() {
        return parkingPort.getTodayStats();
    }

    public void cancelLastScan(String licensePlate) {
        parkingPort.cancelLastScan(licensePlate);
    }

    public List<String> getActiveVehiclesInZone(String zoneCode) {
        return parkingPort.getActiveVehiclesInZone(zoneCode);
    }

    public void clearAllScans() {
        parkingPort.clearAllScans();
    }
}