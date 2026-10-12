package com.turnora.model.entity;

import com.turnora.model.enums.PriorityType;
import com.turnora.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "queue_date", "token_number"}),
        indexes = @Index(name = "idx_ticket_queue", columnList = "service_id, queue_date, status"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Example: F-014
    @Column(name = "token_number", nullable = false, length = 20)
    private String tokenNumber;

    // Number part of the token (14 for F-014). Restarts every day.
    @Column(nullable = false)
    private int sequenceNo;

    @Column(name = "queue_date", nullable = false)
    private LocalDate queueDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceType service;

    // Empty for guest visitors
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String customerName;

    @Column(nullable = false, length = 15)
    private String phone;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PriorityType priorityType = PriorityType.NORMAL;

    @Builder.Default
    @Column(nullable = false)
    private int priorityWeight = 1;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counter_id")
    private Counter counter;

    // How the token was taken: QR, KIOSK or STAFF
    @Builder.Default
    @Column(nullable = false, length = 10)
    private String source = "QR";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime calledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (queueDate == null) {
            queueDate = LocalDate.now();
        }
    }
}