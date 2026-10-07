package com.parkeye.pvs.dto;

import com.parkeye.pvs.model.ParkingStatus;
import java.time.LocalDateTime;

public record ScoutVerificationResponse(
        String licensePlate,
        String zoneCode,
        ParkingStatus status,
        LocalDateTime validUntil,
        String message
) {}