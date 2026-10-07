package com.parkeye.pvs.dto;

import com.parkeye.pvs.model.ParkingStatus;
import java.time.LocalDateTime;

public record ScanHistoryDto(
        String licensePlate,
        String zoneCode,
        String location,
        ParkingStatus status,
        String scannerId, // E.g., "Scout-01" or "Cam-03"
        LocalDateTime scanTime
) {}