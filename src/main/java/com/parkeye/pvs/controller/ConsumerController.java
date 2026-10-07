package com.parkeye.pvs.controller;

import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.dto.ConsumerDtos.*;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.service.ConsumerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/consumer")
@CrossOrigin(origins = "*")
public class ConsumerController {

    private final MunicipalParkingPort parkingPort;
    private final ConsumerService consumerService;

    public ConsumerController(MunicipalParkingPort parkingPort, ConsumerService consumerService) {
        this.parkingPort = parkingPort;
        this.consumerService = consumerService;
    }

    @GetMapping("/zones")
    public ResponseEntity<List<ParkingZone>> getZones() {
        return ResponseEntity.ok(parkingPort.fetchAllZones());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<ConsumerVehicle>> getMyVehicles() {
        return ResponseEntity.ok(consumerService.getMyVehicles());
    }

    @PostMapping("/parking/start")
    public ResponseEntity<ActiveSessionResponse> startParking(@RequestBody StartParkingRequest request) {
        return ResponseEntity.ok(consumerService.startParking(request));
    }
}