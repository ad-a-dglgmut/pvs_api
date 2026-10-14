package com.parkeye.pvs.service;

import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.dto.ConsumerDtos.ZoneDto;
import com.parkeye.pvs.exception.ApiException;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.ZoneCategory;
import com.parkeye.pvs.model.ZoneStatus;
import com.parkeye.pvs.util.GeoUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ZoneService {

    /** A zone with 30% or fewer free spots is shown as "limited" (amber). */
    private static final int LIMITED_PERCENT = 30;

    private final MunicipalParkingPort port;

    public ZoneService(MunicipalParkingPort port) {
        this.port = port;
    }

    public static ZoneStatus statusOf(int capacity, int occupied) {
        int free = Math.max(0, capacity - occupied);
        if (free == 0) return ZoneStatus.FULL;
        if (free * 100 <= capacity * LIMITED_PERCENT) return ZoneStatus.LIMITED;
        return ZoneStatus.AVAILABLE;
    }

    /** Sorted by distance when the caller shares a location, otherwise by zone code. */
    public List<ZoneDto> list(Double lat, Double lng, ZoneCategory category) {
        boolean hasLocation = lat != null && lng != null;
        // zones without coordinates have no distance: they go last
        Comparator<ZoneDto> order = hasLocation
                ? Comparator.comparing(ZoneDto::distanceMeters, Comparator.nullsLast(Comparator.naturalOrder()))
                  .thenComparing(ZoneDto::zoneCode)
                : Comparator.comparing(ZoneDto::zoneCode);
        return port.fetchAllZones().stream()
                .filter(z -> category == null || z.category() == category)
                .map(z -> toDto(z, lat, lng))
                .sorted(order)
                .toList();
    }

    public ZoneDto get(String zoneCode, Double lat, Double lng) {
        ParkingZone zone = port.findZone(zoneCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", "Zone " + zoneCode + " not found."));
        return toDto(zone, lat, lng);
    }

    /** Closest zone that still has free spots ("Најблизок паркинг" card). */
    public ZoneDto nearest(double lat, double lng) {
        return list(lat, lng, null).stream()
                .filter(z -> z.status() != ZoneStatus.FULL)
                .filter(z -> z.distanceMeters() != null)
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NO_ZONE_AVAILABLE", "No zone has free spots right now."));
    }

    ZoneDto toDto(ParkingZone z, Double lat, Double lng) {
        int capacity = z.capacity();
        int occupied = Math.min(z.occupied(), capacity);
        Integer distance = (lat != null && lng != null && z.lat() != null && z.lng() != null)
                ? GeoUtils.distanceMeters(lat, lng, z.lat(), z.lng())
                : null;
        return new ZoneDto(
                z.id(), z.zoneCode(), z.address(), z.category(), z.workingHours(),
                z.hourlyRate(), z.maxDurationHours(), capacity, occupied, capacity - occupied,
                statusOf(capacity, occupied), z.lat(), z.lng(), distance);
    }
}