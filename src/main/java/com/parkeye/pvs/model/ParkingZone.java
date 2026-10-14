package com.parkeye.pvs.model;

import java.math.BigDecimal;

/**
 * Adapter-agnostic view of a parking zone. {@code occupied} is always computed by the
 * adapter from currently active sessions (never stored), so it cannot drift.
 */
public record ParkingZone(
        Long id,
        String zoneCode,
        String name,
        String address,
        ZoneCategory category,
        String workingHours,
        BigDecimal hourlyRate,
        Integer capacity,
        Integer occupied,
        Double lat,
        Double lng
) {}
