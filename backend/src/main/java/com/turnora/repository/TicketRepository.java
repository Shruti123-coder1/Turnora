package com.turnora.repository;

import com.turnora.model.entity.Ticket;
import com.turnora.model.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

// Queue queries (priority order, Call Next with lock) are added in Phase 4.
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByServiceIdAndQueueDateAndStatus(Long serviceId, LocalDate queueDate, TicketStatus status);

    boolean existsByServiceId(Long serviceId);

    boolean existsByCounterId(Long counterId);
}