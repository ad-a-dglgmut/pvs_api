package com.parkeye.pvs.adapter.entity;

import com.parkeye.pvs.model.ZoneCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "zones")
@Getter @Setter @NoArgsConstructor
public class ZoneEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String zoneCode;

    /** Optional free text (e.g. a street hint). Zones are identified by zoneCode only. */
    private String address;

    @Enumerated(EnumType.STRING)
    private ZoneCategory category;

    /** Free text for display, e.g. "07:00 - 21:00". */
    private String workingHours;

    private BigDecimal hourlyRate;

    /** Longest single stay in hours; null = no limit (RED zones: 2). */
    private Integer maxDurationHours;
    private Integer capacity;

    private Double lat;
    private Double lng;

    /**
     * Outline of the zone on the map. A zone can cover several street stretches:
     * stretches are separated by '|', points by ';', and a point is "lat,lng".
     * Example: {@code 41.4362,22.6402;41.4380,22.6409|41.4350,22.6440;41.4348,22.6455}
     */
    @Column(length = 4000)
    private String geometry;
}