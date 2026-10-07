package com.parkeye.pvs.dto;

public record ActiveSessionDto(
        Long ticketId,
        String zoneName,
        String licensePlate,
        String startTime,
        String endTime,
        Double cost,
        Long remainingSeconds
) {}