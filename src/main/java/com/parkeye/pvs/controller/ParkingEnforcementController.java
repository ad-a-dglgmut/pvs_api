package com.parkeye.pvs.controller;

import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationRequest;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.Vehicle;
import com.parkeye.pvs.service.ParkingEnforcementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pvs")
@CrossOrigin(origins = "*") // Allows calls from the Flutter client during local development
public class ParkingEnforcementController {

    private final ParkingEnforcementService enforcementService;

    public ParkingEnforcementController(ParkingEnforcementService enforcementService) {
        this.enforcementService = enforcementService;
    }

    @GetMapping("/zones")
    public ResponseEntity<List<ParkingZone>> getAllZones() {
        return ResponseEntity.ok(enforcementService.getZones());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(enforcementService.getVehicles());
    }

    @PostMapping("/scout/verify")
    public ResponseEntity<ScoutVerificationResponse> verifyParking(@Valid @RequestBody ScoutVerificationRequest request) {
        ScoutVerificationResponse response = enforcementService.verifyParking(request);
        return ResponseEntity.ok(response);
    }

    // 1. Get recent scans for the "Последни Скенирања" list
// ... existing endpoints ...

    @GetMapping("/scans/recent")
    public ResponseEntity<List<ScanHistoryDto>> getRecentScans() {
        return ResponseEntity.ok(enforcementService.getRecentScans());
    }

    @GetMapping("/stats/today")
    public ResponseEntity<DashboardStatsDto> getTodayStats() {
        return ResponseEntity.ok(enforcementService.getTodayStats());
    }

    @DeleteMapping("/scout/scan/{licensePlate}")
    public ResponseEntity<Void> cancelLastScan(@PathVariable String licensePlate) {
        enforcementService.cancelLastScan(licensePlate);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones/{zoneCode}/vehicles")
    public ResponseEntity<List<String>> getActiveVehiclesInZone(@PathVariable String zoneCode) {
        return ResponseEntity.ok(enforcementService.getActiveVehiclesInZone(zoneCode));
    }

    @DeleteMapping("/scout/scans")
    public ResponseEntity<Void> clearAllScans() {
        enforcementService.clearAllScans();
        return ResponseEntity.noContent().build();
    }
}