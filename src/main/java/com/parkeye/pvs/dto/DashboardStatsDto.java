package com.parkeye.pvs.dto;

public record DashboardStatsDto(
        long totalScansToday,
        long paidVehicles,
        long unpaidVehicles,
        double paidPercentage
) {}