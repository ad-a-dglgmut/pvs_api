package com.parkeye.pvs.adapter;

import com.parkeye.pvs.adapter.entity.EnforcementScanEntity;
import com.parkeye.pvs.adapter.entity.ParkingTicketEntity;
import com.parkeye.pvs.adapter.repository.EnforcementScanRepository;
import com.parkeye.pvs.adapter.repository.ParkingTicketRepository; import com.parkeye.pvs.adapter.repository.VehicleRepository;
import com.parkeye.pvs.adapter.repository.ZoneRepository;
import com.parkeye.pvs.dto.DashboardStatsDto;
import com.parkeye.pvs.dto.ScanHistoryDto;
import com.parkeye.pvs.dto.ScoutVerificationResponse;
import com.parkeye.pvs.adapter.entity.ZoneEntity;
import com.parkeye.pvs.adapter.repository.ZoneOccupancy;
import com.parkeye.pvs.model.NewParkingSession;
import com.parkeye.pvs.model.ParkingSession;
import com.parkeye.pvs.model.ParkingStatus;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.PaymentMethod;
import com.parkeye.pvs.model.Vehicle;
import com.parkeye.pvs.model.ZoneCategory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LocalDatabaseParkingAdapter implements MunicipalParkingPort {

    private final ZoneRepository zoneRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingTicketRepository ticketRepository;
    private final EnforcementScanRepository scanRepository;
    private final Clock clock;

    public LocalDatabaseParkingAdapter(ZoneRepository zoneRepository,
                                       VehicleRepository vehicleRepository,
                                       ParkingTicketRepository ticketRepository,
                                       EnforcementScanRepository scanRepository,
                                       Clock clock) {
        this.clock = clock;
        this.zoneRepository = zoneRepository;
        this.vehicleRepository = vehicleRepository;
        this.ticketRepository = ticketRepository;
        this.scanRepository = scanRepository;
    }

    @Override
    public List<ParkingZone> fetchAllZones() {
        Map<String, Integer> occupancy = new HashMap<>();
        for (ZoneOccupancy o : ticketRepository.countActiveTicketsPerZone()) {
            occupancy.put(o.zoneCode(), o.active().intValue());
        }
        return zoneRepository.findAll().stream()
                .map(z -> toZone(z, occupancy.getOrDefault(z.getZoneCode().toUpperCase(), 0)))
                .toList();
    }

    @Override
    public Optional<ParkingZone> findZone(String zoneCode) {
        return zoneRepository.findByZoneCodeIgnoreCase(zoneCode.trim())
                .map(z -> toZone(z, (int) ticketRepository.countActiveTicketsByZone(z.getZoneCode())));
    }

    private ParkingZone toZone(ZoneEntity z, int occupied) {
        return new ParkingZone(
                z.getId(),
                z.getZoneCode(),
                z.getName(),
                z.getAddress() != null ? z.getAddress() : "Струмица",
                z.getCategory() != null ? z.getCategory() : ZoneCategory.YELLOW,
                z.getWorkingHours() != null ? z.getWorkingHours() : "07:00 - 21:00",
                z.getHourlyRate(),
                z.getCapacity() != null ? z.getCapacity() : 100,
                occupied,
                z.getLat() != null ? z.getLat() : 41.4378,
                z.getLng() != null ? z.getLng() : 22.6427
        );
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
            LocalDateTime now = LocalDateTime.now(clock);
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
        scanLog.setScanTime(LocalDateTime.now(clock));
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

    // ------------------------------------------------------------------
    // Consumer sessions
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public ParkingSession createSession(NewParkingSession s) {
        ParkingTicketEntity t = new ParkingTicketEntity();
        t.setLicensePlate(s.licensePlate());
        t.setZoneCode(s.zoneCode());
        t.setStartTime(toLocal(s.startTime()));
        t.setEndTime(toLocal(s.endTime()));
        t.setIsPaid(true); // payment is taken by PaymentService before this is called
        t.setCost(s.cost());
        t.setRefunded(BigDecimal.ZERO);
        t.setPaymentMethod(s.paymentMethod());
        t = ticketRepository.save(t);
        return toSession(t, zoneNames());
    }

    @Override
    public Optional<ParkingSession> findSession(Long sessionId) {
        return ticketRepository.findById(sessionId).map(t -> toSession(t, zoneNames()));
    }

    @Override
    public List<ParkingSession> findSessionsByPlates(Collection<String> licensePlates) {
        if (licensePlates.isEmpty()) return List.of();
        List<String> upper = licensePlates.stream().map(p -> p.trim().toUpperCase()).toList();
        Map<String, String> names = zoneNames();
        return ticketRepository.findByPlates(upper).stream().map(t -> toSession(t, names)).toList();
    }

    @Override
    @Transactional
    public ParkingSession updateSession(Long sessionId, Instant endTime, BigDecimal cost, BigDecimal refunded) {
        ParkingTicketEntity t = ticketRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("Ticket " + sessionId + " vanished"));
        t.setEndTime(toLocal(endTime));
        t.setCost(cost);
        t.setRefunded(refunded);
        t = ticketRepository.save(t);
        return toSession(t, zoneNames());
    }

    private Map<String, String> zoneNames() {
        Map<String, String> names = new HashMap<>();
        zoneRepository.findAll().forEach(z -> names.put(z.getZoneCode().toUpperCase(), z.getName()));
        return names;
    }

    private ParkingSession toSession(ParkingTicketEntity t, Map<String, String> zoneNames) {
        String code = t.getZoneCode();
        return new ParkingSession(
                t.getId(),
                t.getLicensePlate(),
                code,
                zoneNames.getOrDefault(code.toUpperCase(), code),
                toInstant(t.getStartTime()),
                toInstant(t.getEndTime()),
                t.getCost() != null ? t.getCost() : BigDecimal.ZERO,
                t.getRefunded() != null ? t.getRefunded() : BigDecimal.ZERO,
                t.getPaymentMethod() != null ? t.getPaymentMethod() : PaymentMethod.CARD,
                "SR-%05d".formatted(t.getId()),
                Boolean.TRUE.equals(t.getIsPaid())
        );
    }

    // The H2 columns are zone-less LocalDateTime in the server's zone (see PvsApplication).
    private LocalDateTime toLocal(Instant i) {
        return LocalDateTime.ofInstant(i, clock.getZone());
    }

    private Instant toInstant(LocalDateTime l) {
        return l == null ? null : l.atZone(clock.getZone()).toInstant();
    }
}
