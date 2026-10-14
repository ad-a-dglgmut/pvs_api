package com.parkeye.pvs.controller;

import com.parkeye.pvs.dto.ConsumerDtos.*;
import com.parkeye.pvs.model.ZoneCategory;
import com.parkeye.pvs.service.CurrentUser;
import com.parkeye.pvs.service.ParkingSessionService;
import com.parkeye.pvs.service.ZoneService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Driver-facing: zones, parking sessions, history, receipts. */
@RestController
@RequestMapping("/api/v1/consumer")
@CrossOrigin(origins = "*")
public class ConsumerController {

    private final ZoneService zoneService;
    private final ParkingSessionService sessionService;
    private final CurrentUser currentUser;

    public ConsumerController(ZoneService zoneService, ParkingSessionService sessionService, CurrentUser currentUser) {
        this.zoneService = zoneService;
        this.sessionService = sessionService;
        this.currentUser = currentUser;
    }

    // ------------------------------------------------------------------ zones

    /** Optional lat/lng adds distanceMeters and sorts by distance; optional category filters RED/YELLOW. */
    @GetMapping("/zones")
    public ResponseEntity<List<ZoneDto>> getZones(@RequestParam(required = false) Double lat,
                                                  @RequestParam(required = false) Double lng,
                                                  @RequestParam(required = false) ZoneCategory category) {
        return ResponseEntity.ok(zoneService.list(lat, lng, category));
    }

    @GetMapping("/zones/nearest")
    public ResponseEntity<ZoneDto> getNearestZone(@RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(zoneService.nearest(lat, lng));
    }

    @GetMapping("/zones/{zoneCode}")
    public ResponseEntity<ZoneDto> getZone(@PathVariable String zoneCode,
                                           @RequestParam(required = false) Double lat,
                                           @RequestParam(required = false) Double lng) {
        return ResponseEntity.ok(zoneService.get(zoneCode, lat, lng));
    }

    // --------------------------------------------------------------- sessions

    @PostMapping("/parking/start")
    public ResponseEntity<SessionDto> startParking(@Valid @RequestBody StartParkingRequest request) {
        return ResponseEntity.ok(sessionService.start(currentUser.id(), request));
    }

    @GetMapping("/parking/active")
    public ResponseEntity<List<SessionDto>> getActiveSessions() {
        return ResponseEntity.ok(sessionService.active(currentUser.id()));
    }

    @PostMapping("/parking/{sessionId}/extend")
    public ResponseEntity<SessionDto> extend(@PathVariable long sessionId,
                                             @Valid @RequestBody(required = false) ExtendRequest request) {
        return ResponseEntity.ok(sessionService.extend(currentUser.id(), sessionId, request));
    }

    @PostMapping("/parking/{sessionId}/stop")
    public ResponseEntity<SessionDto> stop(@PathVariable long sessionId) {
        return ResponseEntity.ok(sessionService.stop(currentUser.id(), sessionId));
    }

    // ------------------------------------------------------ history & receipts

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory() {
        return ResponseEntity.ok(sessionService.history(currentUser.id()));
    }

    @GetMapping("/history/{sessionId}/receipt")
    public ResponseEntity<ReceiptDto> getReceipt(@PathVariable long sessionId) {
        return ResponseEntity.ok(sessionService.receipt(currentUser.id(), sessionId));
    }
}
