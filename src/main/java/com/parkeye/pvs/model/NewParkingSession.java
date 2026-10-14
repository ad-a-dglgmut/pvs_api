package com.parkeye.pvs.model;

import java.math.BigDecimal;
import java.time.Instant;

public record NewParkingSession(
        String licensePlate,
        String zoneCode,
        Instant startTime,
        Instant endTime,
        BigDecimal cost,
        PaymentMethod paymentMethod
) {}
