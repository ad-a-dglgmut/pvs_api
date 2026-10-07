package com.parkeye.pvs.adapter.entity;

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

    private String name;
    private BigDecimal hourlyRate;

    private Integer capacity;
    private Integer occupied;

    private Double lat;
    private Double lng;
}