package com.parkeye.pvs.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String title;
    private String body;
    private LocalDateTime createdAt;
    private Boolean seen;

    /** Optional link to the session + a kind ("EXPIRING") so the scheduler notifies only once. */
    private Long ticketId;
    private String kind;
}
