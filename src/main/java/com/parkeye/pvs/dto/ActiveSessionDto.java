package com.parkeye.pvs.dto;

public record ActiveSessionDto(
        Long ticketId,
        String zoneCode,
        String licensePlate,
        String startTime,
        String endTime,
        Double cost,
        Long remainingSeconds
) {}