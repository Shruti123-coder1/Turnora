package com.turnora.model.entity;

import com.turnora.model.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Every status change of a ticket is saved here. This is the queue history.
@Entity
@Table(name = "ticket_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // CREATED, CALLED, STARTED, COMPLETED, NO_SHOW, CANCELLED, TRANSFERRED
    @Column(nullable = false, length = 30)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TicketStatus toStatus;

    private Long counterId;

    // User who did the action (staff or admin). Empty for system or guest actions.
    private Long actorId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}