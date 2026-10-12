package com.turnora.model.entity;

import jakarta.persistence.*;
import lombok.*;

// One college office (Accounts Section, Exam Section, Library ...)
@Entity
@Table(name = "services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    // Token prefix, for example F for the Accounts Section (F-001, F-002 ...)
    @Column(nullable = false, unique = true, length = 5)
    private String prefix;

    @Column(length = 255)
    private String description;

    // Average time to serve one person. It is updated after every completed ticket.
    @Builder.Default
    @Column(nullable = false)
    private double avgServiceMinutes = 5.0;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}