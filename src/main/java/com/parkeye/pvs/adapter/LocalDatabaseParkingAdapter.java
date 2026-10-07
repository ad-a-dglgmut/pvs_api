package com.parkeye.pvs.adapter;

import com.parkeye.pvs.adapter.entity.EnforcementScanEntity;
import com.parkeye.pvs.adapter.entity.ParkingTicketEntity;
import com.parkeye.pvs.adapter.repository.EnforcementScanRepository;
import com.parkeye.pvs.adapter.repository.ParkingTicketRepository; import com.parkeye.pvs.adapter.repository.VehicleRepository;
import com.parkeye.pvs.adapter.repository.ZoneRepository;
import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.model.ParkingStatus;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.Vehicle;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Component
public class LocalDatabaseParkingAdapter implements MunicipalParkingPort {

    private final ZoneRepository zoneRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingTicketRepository ticketRepository;
    private final EnforcementScanRepository scanRepository;

    public LocalDatabaseParkingAdapter(ZoneRepository zoneRepository,
                                       VehicleRepository vehicleRepository,
                                       ParkingTicketRepository ticketRepository,
                                       EnforcementScanRepository scanRepository) {
        this.zoneRepository = zoneRepository;
        this.vehicleRepository = vehicleRepository;
        this.ticketRepository = ticketRepository;
        this.scanRepository = scanRepository;
    }

    @Override
    public List<ParkingZone> fetchAllZones() {
        return zoneRepository.findAll().stream()
                .map(z -> new ParkingZone(
                        z.getId(),
                        z.getZoneCode(),
                        z.getName(),
                        z.getHourlyRate(),
                        // Add null fallbacks in case your H2 database doesn't have these populated yet
                        z.getCapacity() != null ? z.getCapacity() : 100,
                        z.getOccupied() != null ? z.getOccupied() : 0,
                        z.getLat() != null ? z.getLat() : 41.4378,
                        z.getLng() != null ? z.getLng() : 22.6427
                ))
                .toList();
    }

    @Override
    public List<Vehicle> fetchAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(v -> new Vehicle(
                        v.getId(),
                        v.getLicensePlate(),
                        v.getMake(),
                        v.getModel(),
                        v.getColor()
                ))
                .toList();
    }

    @Override
    public ScoutVerificationResponse verifyParking(String licensePlate, String zoneCode) {
        String cleanPlate = licensePlate.trim().toUpperCase();
        String cleanZone = zoneCode.trim().toUpperCase();

        List<ParkingTicketEntity> tickets = ticketRepository.findByLicensePlateIgnoreCaseOrderByEndTimeDesc(cleanPlate);

        ParkingStatus finalStatus;
        LocalDateTime validUntil = null;
        String message;

        if (tickets.isEmpty()) {
            finalStatus = ParkingStatus.NOT_FOUND;
            message = "No ticket history found for vehicle.";
        } else {
            LocalDateTime now = LocalDateTime.now();
            // Check if there is an active session
            Optional<ParkingTicketEntity> activeTicket = tickets.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getIsPaid()) && t.getEndTime().isAfter(now))
                    .findFirst();

            if (activeTicket.isEmpty()) {
                ParkingTicketEntity latest = tickets.get(0);
                finalStatus = ParkingStatus.EXPIRED;
                validUntil = latest.getEndTime();
                message = "Parking session expired or unpaid.";
            } else {
                ParkingTicketEntity ticket = activeTicket.get();
                validUntil = ticket.getEndTime();

                if (!ticket.getZoneCode().equalsIgnoreCase(cleanZone)) {
                    finalStatus = ParkingStatus.WRONG_ZONE;
                    message = "Paid for zone " + ticket.getZoneCode() + ", but scanned in " + cleanZone;
                } else {
                    finalStatus = ParkingStatus.VALID;
                    message = "Parking is valid and paid.";
                }
            }
        }

        // Automatically log the scan event for the dashboard analytics
        EnforcementScanEntity scanLog = new EnforcementScanEntity();
        scanLog.setLicensePlate(cleanPlate);
        scanLog.setZoneCode(cleanZone);
        scanLog.setStatus(finalStatus);
        scanLog.setScanTime(LocalDateTime.now());
        scanLog.setScannerId("Scout-Mobile-01"); // Future: Extract this dynamically from Auth context
        scanLog.setLocation("Струмица - " + cleanZone);
        scanRepository.save(scanLog);

        return new ScoutVerificationResponse(cleanPlate, cleanZone, finalStatus, validUntil, message);
    }

    @Override
    public List<ScanHistoryDto> getRecentScans() {
        return scanRepository.findTop15ByOrderByScanTimeDesc().stream()
                .map(s -> new ScanHistoryDto(
                        s.getLicensePlate(),
                        s.getZoneCode(),
                        s.getLocation(),
                        s.getStatus(),
                        s.getScannerId(),
                        s.getScanTime()
                ))
                .toList();
    }

    @Override
    public DashboardStatsDto getTodayStats() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        long total = scanRepository.countByScanTimeBetween(startOfDay, endOfDay);
        long paid = scanRepository.countByScanTimeBetweenAndStatus(startOfDay, endOfDay, ParkingStatus.VALID);
        long unpaid = total - paid;

        double percentage = total == 0 ? 0.0 : ((double) paid / total) * 100;
        // Round to 1 decimal place for the UI
        percentage = Math.round(percentage * 10.0) / 10.0;

        return new DashboardStatsDto(total, paid, unpaid, percentage);
    }

    @Override
    public void cancelLastScan(String licensePlate) {
        String cleanPlate = licensePlate.trim().toUpperCase();
        // Finds the most recent scan for this exact plate and removes it from the database
        scanRepository.findTopByLicensePlateOrderByScanTimeDesc(cleanPlate)
                .ifPresent(scanRepository::delete);
    }

    @Override
    public List<String> getActiveVehiclesInZone(String zoneCode) {
        return ticketRepository.findActivePlatesByZone(zoneCode);
    }

    @Override
    public void clearAllScans() {
        scanRepository.deleteAll();
    }
}