package com.parkeye.pvs.adapter.repository;

/** Projection: number of currently active paid tickets per (upper-cased) zone code. */
public record ZoneOccupancy(String zoneCode, Long active) {}
