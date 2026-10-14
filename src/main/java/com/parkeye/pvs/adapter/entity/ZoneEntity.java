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
}