package com.parkeye.pvs.dto;

public record StartParkingRequest(
        String licensePlate,
        String zoneCode,
        Integer hours,
        String paymentMethod
) {}