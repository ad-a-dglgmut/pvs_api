package com.parkeye.pvs.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Display metadata of a tokenised card. Never store the PAN/CVV here - the PSP keeps those. */
@Entity
@Table(name = "saved_cards")
@Getter @Setter @NoArgsConstructor
public class SavedCardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String brand;
    private String last4;
    private Integer expiryMonth;
    private Integer expiryYear;
    private Boolean primaryCard;
}
