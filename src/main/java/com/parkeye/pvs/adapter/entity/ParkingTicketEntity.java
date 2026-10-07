package com.parkeye.pvs.adapter.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_tickets")
@Getter @Setter @NoArgsConstructor
public class ParkingTicketEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String licensePlate;

    @Column(nullable = false)
    private String zoneCode;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isPaid;
}