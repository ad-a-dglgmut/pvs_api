package com.parkeye.pvs.model;

public record Vehicle(
        Long id,
        String licensePlate,
        String make,
        String model,
        String color
) {}