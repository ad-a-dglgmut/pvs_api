package com.parkeye.pvs.dto;

import jakarta.validation.constraints.NotBlank;

public record ScoutVerificationRequest(
        @NotBlank(message = "License plate is required")
        String licensePlate,

        @NotBlank(message = "Zone code is required")
        String zoneCode
) {}