package com.parkeye.pvs.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adapter-agnostic view of a parking zone. {@code occupied} is always computed by the
 * adapter from currently active sessions (never stored), so it cannot drift.
 */
public record ParkingZone(
        Long id,
        String zoneCode,
        String address,          // optional free text; null when unknown
        ZoneCategory category,
        String workingHours,
        BigDecimal hourlyRate,
        Integer maxDurationHours, // null = no limit
        Integer capacity,
        Integer occupied,
        Double lat,               // label position; null when the zone has neither a point nor a path
        Double lng,
        List<List<GeoPoint>> paths // the street stretches the zone covers (empty = not drawn yet)
) {}