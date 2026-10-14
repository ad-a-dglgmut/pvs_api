package com.parkeye.pvs.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vehicles")
@Getter @Setter @NoArgsConstructor
public class UserVehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String licensePlate;

    private String make;
    private String model;
    private String nickname;

    private Boolean defaultVehicle;
}
