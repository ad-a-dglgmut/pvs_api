package com.parkeye.pvs.model;

import java.math.BigDecimal;

public record ParkingZone(
        Long id,
        String zoneCode,
        String name,
        BigDecimal hourlyRate,
        Integer capacity,
        Integer occupied,
        Double lat,
        Double lng
) {}