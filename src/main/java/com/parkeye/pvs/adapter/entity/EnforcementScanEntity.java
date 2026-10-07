package com.parkeye.pvs.adapter.entity;

import com.parkeye.pvs.model.ParkingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "enforcement_scans")
@Getter @Setter @NoArgsConstructor
public class EnforcementScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String licensePlate;
    private String zoneCode;

    @Enumerated(EnumType.STRING)
    private ParkingStatus status;

    private LocalDateTime scanTime;
    private String scannerId;
    private String location;
}