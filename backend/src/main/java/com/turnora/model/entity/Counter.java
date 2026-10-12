package com.turnora.model.entity;

import com.turnora.model.enums.CounterStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "counters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CounterStatus status = CounterStatus.ACTIVE;

    // Staff member who works at this counter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    // Ticket that this counter is handling right now
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_ticket_id")
    private Ticket currentTicket;

    // Offices that this counter can serve (table: counter_services)
    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "counter_services",
            joinColumns = @JoinColumn(name = "counter_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<ServiceType> services = new HashSet<>();
}