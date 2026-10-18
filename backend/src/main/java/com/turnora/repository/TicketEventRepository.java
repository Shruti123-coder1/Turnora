package com.turnora.repository;

import com.turnora.model.entity.TicketEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketEventRepository extends JpaRepository<TicketEvent, Long> {

    // The full history of one ticket, oldest first
    List<TicketEvent> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
