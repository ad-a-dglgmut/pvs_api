package com.parkeye.pvs.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Adapter-agnostic parking session (a "ticket" in the municipal system).
 * {@code cost} is what the driver is currently charged; {@code refunded} is what was
 * given back when the session was ended early.
 */
public record ParkingSession(
        Long id,
        String licensePlate,
        String zoneCode,
        Instant startTime,
        Instant endTime,
        BigDecimal cost,
        BigDecimal refunded,
        PaymentMethod paymentMethod,
        String receiptNo,
        boolean paid
) {
    public boolean isActiveAt(Instant now) {
        return paid && endTime != null && endTime.isAfter(now);
    }
}