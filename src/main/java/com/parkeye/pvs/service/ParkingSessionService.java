package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.NotificationType;
import com.parkeye.pvs.account.entity.UserVehicleEntity;
import com.parkeye.pvs.account.repository.UserVehicleRepository;
import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.dto.ConsumerDtos.*;
import com.parkeye.pvs.exception.ApiException;
import com.parkeye.pvs.model.NewParkingSession;
import com.parkeye.pvs.model.ParkingSession;
import com.parkeye.pvs.model.ParkingZone;
import com.parkeye.pvs.model.PaymentMethod;
import com.parkeye.pvs.model.ZoneStatus;
import com.parkeye.pvs.util.PlateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Driver-side parking flow: start / extend / stop / active / history / receipt.
 * All municipal data goes through {@link MunicipalParkingPort}; this class never touches
 * the municipal tables directly.
 */
@Service
public class ParkingSessionService {

    private final MunicipalParkingPort port;
    private final UserVehicleRepository vehicles;
    private final PaymentService payments;
    private final NotificationService notifications;
    private final Clock clock;
    private final int maxSessionHours;
    private final String issuerName;

    public ParkingSessionService(MunicipalParkingPort port,
                                 UserVehicleRepository vehicles,
                                 PaymentService payments,
                                 NotificationService notifications,
                                 Clock clock,
                                 @Value("${parkeye.parking.max-session-hours:12}") int maxSessionHours,
                                 @Value("${parkeye.receipt.issuer:ЈКП Паркиралишта Струмица}") String issuerName) {
        this.port = port;
        this.vehicles = vehicles;
        this.payments = payments;
        this.notifications = notifications;
        this.clock = clock;
        this.maxSessionHours = maxSessionHours;
        this.issuerName = issuerName;
    }

    // ------------------------------------------------------------------ start

    @Transactional
    public SessionDto start(long userId, StartParkingRequest req) {
        String plate = PlateUtil.normalize(req.licensePlate());
        if (!userPlates(userId).contains(plate)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "VEHICLE_NOT_OWNED", "That vehicle is not on your account.");
        }

        ParkingZone zone = port.findZone(req.zoneCode())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", "Zone not found."));
        if (ZoneService.statusOf(zone.capacity(), zone.occupied()) == ZoneStatus.FULL) {
            throw new ApiException(HttpStatus.CONFLICT, "ZONE_FULL", "This zone has no free spots.");
        }
        if (zone.maxDurationHours() != null && req.hours() > zone.maxDurationHours()) {
            throw new ApiException(HttpStatus.CONFLICT, "MAX_DURATION_EXCEEDED",
                    "Zone " + zone.zoneCode() + " allows at most " + zone.maxDurationHours() + " hours.");
        }

        Instant now = clock.instant();
        boolean alreadyParked = port.findSessionsByPlates(List.of(plate)).stream().anyMatch(s -> s.isActiveAt(now));
        if (alreadyParked) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_ALREADY_ACTIVE",
                    "This vehicle already has an active parking session.");
        }

        PaymentMethod method = PaymentMethod.parse(req.paymentMethod());
        BigDecimal cost = zone.hourlyRate().multiply(BigDecimal.valueOf(req.hours()));

        payments.charge(userId, method, cost, "park:" + plate + ":" + zone.zoneCode());
        ParkingSession created = port.createSession(new NewParkingSession(
                plate, zone.zoneCode(), now, now.plusSeconds(req.hours() * 3600L), cost, method));

        notifications.create(userId, NotificationType.SUCCESS, "Успешна наплата",
                "Наплатени се " + money(cost) + " ден. преку " + payments.describe(userId, method) + ".",
                created.id(), "PAID");
        return toDto(created, now);
    }

    // ----------------------------------------------------------------- extend

    @Transactional
    public SessionDto extend(long userId, long sessionId, ExtendRequest req) {
        Instant now = clock.instant();
        ParkingSession s = ownedActive(userId, sessionId, now);

        ParkingZone zone = port.findZone(s.zoneCode())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ZONE_NOT_FOUND", "Zone not found."));

        int hours = (req == null || req.hours() == null) ? 1 : req.hours();
        Instant newEnd = s.endTime().plusSeconds(hours * 3600L);
        // the zone's own limit (red zones: 2 h) applies on top of the global cap
        int limitHours = zone.maxDurationHours() == null
                ? maxSessionHours
                : Math.min(maxSessionHours, zone.maxDurationHours());
        if (Duration.between(s.startTime(), newEnd).getSeconds() > limitHours * 3600L) {
            throw new ApiException(HttpStatus.CONFLICT, "MAX_DURATION_EXCEEDED",
                    "A session in zone " + zone.zoneCode() + " can last at most " + limitHours + " hours.");
        }
        BigDecimal extra = zone.hourlyRate().multiply(BigDecimal.valueOf(hours));
        PaymentMethod method = (req == null || req.paymentMethod() == null)
                ? s.paymentMethod()
                : PaymentMethod.parse(req.paymentMethod());

        payments.charge(userId, method, extra, "extend:" + sessionId);
        ParkingSession updated = port.updateSession(sessionId, newEnd, s.cost().add(extra), s.refunded());

        notifications.create(userId, NotificationType.SUCCESS, "Паркирањето е продолжено",
                "Додадени " + hours + " ч. на " + s.zoneCode() + ". Наплатени се " + money(extra)
                        + " ден. преку " + payments.describe(userId, method) + ".",
                sessionId, "EXTENDED");
        return toDto(updated, now);
    }

    // ------------------------------------------------------------------- stop

    /**
     * Ends the session now. The driver pays pro-rata for the minutes actually used
     * (rounded up to a whole minute, to whole denars) and the rest is refunded.
     */
    @Transactional
    public SessionDto stop(long userId, long sessionId) {
        Instant now = clock.instant();
        ParkingSession s = ownedActive(userId, sessionId, now);

        long totalMinutes = Math.max(1, Duration.between(s.startTime(), s.endTime()).toMinutes());
        long usedSeconds = Math.max(0, Duration.between(s.startTime(), now).getSeconds());
        long billedMinutes = Math.min(totalMinutes, Math.max(1, (usedSeconds + 59) / 60));

        BigDecimal finalCost = s.cost()
                .multiply(BigDecimal.valueOf(billedMinutes))
                .divide(BigDecimal.valueOf(totalMinutes), 0, RoundingMode.HALF_UP)
                .min(s.cost());
        BigDecimal refund = s.cost().subtract(finalCost);

        ParkingSession updated = port.updateSession(sessionId, now, finalCost, s.refunded().add(refund));
        if (refund.signum() > 0) {
            payments.refund(userId, s.paymentMethod(), refund, "stop:" + sessionId);
        }

        String body = "Паркирањето на " + s.zoneCode() + " заврши. Наплатено: " + money(finalCost) + " ден."
                + (refund.signum() > 0 ? " Вратени: " + money(refund) + " ден." : "");
        notifications.create(userId, NotificationType.INFO, "Паркирањето е завршено", body, sessionId, "STOPPED");
        return toDto(updated, now);
    }

    // ----------------------------------------------------------------- reads

    @Transactional(readOnly = true)
    public List<SessionDto> active(long userId) {
        Instant now = clock.instant();
        return port.findSessionsByPlates(userPlates(userId)).stream()
                .filter(s -> s.isActiveAt(now))
                .sorted(Comparator.comparing(ParkingSession::endTime))
                .map(s -> toDto(s, now))
                .toList();
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(long userId) {
        Instant now = clock.instant();
        YearMonth thisMonth = YearMonth.now(clock);
        List<ParkingSession> paid = port.findSessionsByPlates(userPlates(userId)).stream()
                .filter(ParkingSession::paid)
                .toList();

        List<HistoryItemDto> items = paid.stream()
                .filter(s -> !s.isActiveAt(now))
                .map(s -> new HistoryItemDto(
                        s.id(), s.licensePlate(), s.zoneCode(), s.startTime(), s.endTime(),
                        durationMinutes(s), s.cost(), s.refunded(), s.paymentMethod(), s.receiptNo()))
                .toList();

        BigDecimal thisMonthSpent = paid.stream()
                .filter(s -> YearMonth.from(s.startTime().atZone(clock.getZone())).equals(thisMonth))
                .map(ParkingSession::cost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saved = paid.stream()
                .map(ParkingSession::refunded)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new HistoryResponse(new HistoryStatsDto(items.size(), thisMonthSpent, saved), items);
    }

    @Transactional(readOnly = true)
    public ReceiptDto receipt(long userId, long sessionId) {
        ParkingSession s = owned(userId, sessionId);
        return new ReceiptDto(s.receiptNo(), issuerName, s.startTime(), s.licensePlate(), s.zoneCode(),
                s.startTime(), s.endTime(), durationMinutes(s), s.paymentMethod(), s.cost());
    }

    // ---------------------------------------------------------------- helpers

    private Set<String> userPlates(long userId) {
        return vehicles.findByUserIdOrderByIdAsc(userId).stream()
                .map(UserVehicleEntity::getLicensePlate)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    /** 404 (not 403) for someone else's session so ids can't be probed. */
    private ParkingSession owned(long userId, long sessionId) {
        ParkingSession s = port.findSession(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found."));
        if (!userPlates(userId).contains(s.licensePlate().toUpperCase())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "SESSION_NOT_FOUND", "Session not found.");
        }
        return s;
    }

    private ParkingSession ownedActive(long userId, long sessionId, Instant now) {
        ParkingSession s = owned(userId, sessionId);
        if (!s.isActiveAt(now)) {
            throw new ApiException(HttpStatus.CONFLICT, "SESSION_NOT_ACTIVE", "This parking session is no longer active.");
        }
        return s;
    }

    private SessionDto toDto(ParkingSession s, Instant now) {
        long total = Math.max(0, Duration.between(s.startTime(), s.endTime()).getSeconds());
        long remaining = Math.max(0, Duration.between(now, s.endTime()).getSeconds());
        return new SessionDto(s.id(), s.licensePlate(), s.zoneCode(), s.startTime(), s.endTime(),
                s.cost(), s.paymentMethod(), s.receiptNo(), total, remaining, now);
    }

    private long durationMinutes(ParkingSession s) {
        return Math.max(0, Duration.between(s.startTime(), s.endTime()).toMinutes());
    }

    private static String money(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}