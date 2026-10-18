package com.turnora.service;

import com.turnora.dto.AdminDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.Counter;
import com.turnora.model.entity.Ticket;
import com.turnora.model.entity.TicketEvent;
import com.turnora.model.entity.User;
import com.turnora.model.enums.TicketStatus;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.TicketEventRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Full queue history for the admin, with filters and pages
@Service
@RequiredArgsConstructor
public class HistoryService {

    @PersistenceContext
    private EntityManager entityManager;

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final CounterRepository counterRepository;
    private final UserRepository userRepository;

    // Filters are optional. Dates default to today.
    @Transactional(readOnly = true)
    public AdminDto.HistoryPage search(LocalDate from, LocalDate to, Long serviceId, String status,
                                       String text, int page, int size) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : LocalDate.now();
        if (start.isAfter(end)) {
            throw AppException.badRequest("The 'from' date must be before the 'to' date");
        }
        if (ChronoUnit.DAYS.between(start, end) > 366) {
            throw AppException.badRequest("Choose a date range of one year or less");
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        // Only the filters that are used are added, so the query stays simple and safe
        StringBuilder where = new StringBuilder(" where t.queueDate between :from and :to");
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("from", start);
        params.put("to", end);

        if (serviceId != null) {
            where.append(" and t.service.id = :serviceId");
            params.put("serviceId", serviceId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" and t.status = :status");
            params.put("status", parseStatus(status));
        }
        if (text != null && !text.isBlank()) {
            where.append(" and (upper(t.tokenNumber) like :text or upper(t.customerName) like :text or t.phone like :text)");
            params.put("text", "%" + text.trim().toUpperCase() + "%");
        }

        TypedQuery<Long> countQuery = entityManager.createQuery(
                "select count(t) from Ticket t" + where, Long.class);
        for (Map.Entry<String, Object> p : params.entrySet()) {
            countQuery.setParameter(p.getKey(), p.getValue());
        }
        long total = countQuery.getSingleResult();

        TypedQuery<Ticket> rowQuery = entityManager.createQuery(
                "select t from Ticket t join fetch t.service left join fetch t.counter" + where
                        + " order by t.createdAt desc, t.id desc", Ticket.class);
        for (Map.Entry<String, Object> p : params.entrySet()) {
            rowQuery.setParameter(p.getKey(), p.getValue());
        }
        rowQuery.setFirstResult(safePage * safeSize);
        rowQuery.setMaxResults(safeSize);

        List<AdminDto.HistoryRow> rows = rowQuery.getResultList().stream().map(this::toRow).toList();
        int totalPages = (int) Math.ceil(total / (double) safeSize);

        return new AdminDto.HistoryPage(rows, safePage, safeSize, total, totalPages);
    }

    // Every status change of one ticket (created, called, started, completed ...)
    @Transactional(readOnly = true)
    public List<AdminDto.EventRow> events(Long ticketId) {
        if (!ticketRepository.existsById(ticketId)) {
            throw AppException.notFound("Token not found");
        }

        Map<Long, String> counterNames = new HashMap<>();
        Map<Long, String> userNames = new HashMap<>();

        return ticketEventRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(e -> new AdminDto.EventRow(
                        e.getId(),
                        e.getEventType(),
                        e.getFromStatus() != null ? e.getFromStatus().name() : null,
                        e.getToStatus() != null ? e.getToStatus().name() : null,
                        counterName(e, counterNames),
                        actorName(e, userNames),
                        e.getCreatedAt()))
                .toList();
    }

    private String counterName(TicketEvent e, Map<Long, String> cache) {
        if (e.getCounterId() == null) {
            return null;
        }
        return cache.computeIfAbsent(e.getCounterId(),
                id -> counterRepository.findById(id).map(Counter::getName).orElse("Counter " + id));
    }

    private String actorName(TicketEvent e, Map<Long, String> cache) {
        if (e.getActorId() == null) {
            return null;
        }
        return cache.computeIfAbsent(e.getActorId(),
                id -> userRepository.findById(id).map(User::getFullName).orElse("User " + id));
    }

    private TicketStatus parseStatus(String status) {
        try {
            return TicketStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest(
                    "Status must be WAITING, CALLED, SERVING, COMPLETED, NO_SHOW or CANCELLED");
        }
    }

    private AdminDto.HistoryRow toRow(Ticket t) {
        Double wait = t.getCalledAt() != null ? minutes(t.getCreatedAt(), t.getCalledAt()) : null;

        Double service = null;
        if (t.getStatus() == TicketStatus.COMPLETED && t.getCompletedAt() != null) {
            LocalDateTime began = t.getStartedAt() != null ? t.getStartedAt() : t.getCalledAt();
            if (began != null) {
                service = minutes(began, t.getCompletedAt());
            }
        }

        return new AdminDto.HistoryRow(
                t.getId(),
                t.getTokenNumber(),
                t.getService().getName(),
                t.getCustomerName(),
                t.getPhone(),
                t.getPriorityType().name(),
                t.getStatus().name(),
                t.getCounter() != null ? t.getCounter().getName() : null,
                t.getSource(),
                t.getQueueDate(),
                t.getCreatedAt(),
                t.getCalledAt(),
                t.getCompletedAt(),
                wait,
                service);
    }

    private double minutes(LocalDateTime from, LocalDateTime to) {
        double value = Duration.between(from, to).toMillis() / 60000.0;
        return Math.round(value * 10.0) / 10.0;
    }
}
