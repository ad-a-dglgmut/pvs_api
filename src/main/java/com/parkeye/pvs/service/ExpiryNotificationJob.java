package com.parkeye.pvs.service;

import com.parkeye.pvs.account.entity.NotificationType;
import com.parkeye.pvs.account.entity.UserEntity;
import com.parkeye.pvs.account.entity.UserVehicleEntity;
import com.parkeye.pvs.account.repository.UserRepository;
import com.parkeye.pvs.account.repository.UserVehicleRepository;
import com.parkeye.pvs.adapter.MunicipalParkingPort;
import com.parkeye.pvs.model.ParkingSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Once a minute: warn drivers whose session ends within 15 minutes (once per session end time). */
@Component
public class ExpiryNotificationJob {
    private static final Logger log = LoggerFactory.getLogger(ExpiryNotificationJob.class);
    private static final long WARN_SECONDS = 15 * 60;

    private final UserRepository users;
    private final UserVehicleRepository vehicles;
    private final MunicipalParkingPort port;
    private final NotificationService notifications;
    private final Clock clock;

    public ExpiryNotificationJob(UserRepository users, UserVehicleRepository vehicles, MunicipalParkingPort port,
                                 NotificationService notifications, Clock clock) {
        this.users = users;
        this.vehicles = vehicles;
        this.port = port;
        this.notifications = notifications;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 60_000, initialDelay = 20_000)
    public void warnExpiring() {
        Instant now = clock.instant();
        for (UserEntity user : users.findAll()) {
            try {
                List<String> plates = vehicles.findByUserIdOrderByIdAsc(user.getId()).stream()
                        .map(UserVehicleEntity::getLicensePlate).toList();
                for (ParkingSession s : port.findSessionsByPlates(plates)) {
                    if (!s.isActiveAt(now)) continue;
                    long left = Duration.between(now, s.endTime()).getSeconds();
                    if (left > WARN_SECONDS) continue;
                    // kind contains the end time, so an extended session can warn again later
                    notifications.createOnce(user.getId(), NotificationType.WARNING,
                            "Паркирање во Струмица",
                            "Вашата сесија на " + s.zoneCode() + " истекува наскоро.",
                            s.id(), "EXPIRING@" + s.endTime().getEpochSecond());
                }
            } catch (RuntimeException e) {
                log.warn("Expiry check failed for user {}", user.getId(), e);
            }
        }
    }
}
