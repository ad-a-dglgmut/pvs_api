package com.parkeye.pvs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConsumerDtos {

    public record StartParkingRequest(
            String licensePlate,
            String zoneCode,
            int hours,
            String paymentMethod
    ) {}

    public record ActiveSessionResponse(
            Long ticketId,
            String licensePlate,
            String zoneCode,
            String zoneName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal totalCost
    ) {}

    public record ConsumerVehicle(
            String licensePlate,
            String make,
            String model
    ) {}
}