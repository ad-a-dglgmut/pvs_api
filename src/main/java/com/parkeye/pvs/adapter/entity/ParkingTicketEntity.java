package com.parkeye.pvs.adapter.entity;

import com.parkeye.pvs.model.PaymentMethod;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
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

    /** Amount currently charged for this ticket (grows on extend, shrinks on early stop). */
    private BigDecimal cost;
    /** Amount given back when the ticket was ended early. */
    private BigDecimal refunded;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
}
