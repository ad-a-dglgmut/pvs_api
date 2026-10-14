package com.parkeye.pvs.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** ParkEye's own user record (NOT part of the municipal system). */
@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true)
    private String email;

    private Boolean verified;
    private String municipality;
    private String language;
    private Boolean smsNotifications;

    private BigDecimal walletBalance;

    /** Optimistic lock so two simultaneous payments can't both spend the same balance. */
    @Version
    private Long version;
}
