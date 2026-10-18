package com.turnora.service;

import com.turnora.dto.AdminDto;
import com.turnora.model.entity.Ticket;
import com.turnora.model.enums.TicketStatus;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Numbers for the admin dashboard: today's totals, busiest hours, per-office and per-counter tables
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final CounterRepository counterRepository;
    private final ServiceTypeRepository serviceRepository;

    // date = null means today
    @Transactional(readOnly = true)
    public AdminDto.AnalyticsResponse analytics(LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        List<Ticket> tickets = ticketRepository.findForDay(day);

        long waiting = countStatus(tickets, TicketStatus.WAITING);
        long inProgress = countStatus(tickets, TicketStatus.CALLED) + countStatus(tickets, TicketStatus.SERVING);
        long completed = countStatus(tickets, TicketStatus.COMPLETED);
        long noShow = countStatus(tickets, TicketStatus.NO_SHOW);
        long cancelled = countStatus(tickets, TicketStatus.CANCELLED);

        // Tokens per hour (busiest hour of the campus)
        long[] perHour = new long[24];
        for (Ticket t : tickets) {
            perHour[t.getCreatedAt().getHour()]++;
        }
        List<AdminDto.HourCount> hours = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            hours.add(new AdminDto.HourCount(h, perHour[h]));
        }

        // Per office
        List<AdminDto.OfficeStat> offices = serviceRepository.findAllByOrderByIdAsc().stream()
                .map(s -> {
                    List<Ticket> mine = tickets.stream()
                            .filter(t -> t.getService().getId().equals(s.getId()))
                            .toList();
                    return new AdminDto.OfficeStat(
                            s.getId(), s.getName(), s.getPrefix(),
                            mine.size(),
                            countStatus(mine, TicketStatus.COMPLETED),
                            averageWait(mine),
                            averageService(mine));
                })
                .toList();

        // Per counter (completed tickets only)
        List<AdminDto.CounterStat> counters = counterRepository.findAllByOrderByIdAsc().stream()
                .map(c -> {
                    List<Ticket> served = tickets.stream()
                            .filter(t -> t.getStatus() == TicketStatus.COMPLETED
                                    && t.getCounter() != null
                                    && t.getCounter().getId().equals(c.getId()))
                            .toList();
                    return new AdminDto.CounterStat(c.getId(), c.getName(), served.size(), averageService(served));
                })
                .toList();

        return new AdminDto.AnalyticsResponse(
                day, tickets.size(), waiting, inProgress, completed, noShow, cancelled,
                averageWait(tickets), averageService(tickets),
                hours, offices, counters);
    }

    private long countStatus(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream().filter(t -> t.getStatus() == status).count();
    }

    // Waiting time = time from joining the queue until the token was called
    private Double waitMinutes(Ticket t) {
        if (t.getCalledAt() == null) {
            return null;
        }
        return minutesBetween(t.getCreatedAt(), t.getCalledAt());
    }

    // Service time = time from start (or call) until completed
    private Double serviceMinutes(Ticket t) {
        if (t.getStatus() != TicketStatus.COMPLETED || t.getCompletedAt() == null) {
            return null;
        }
        LocalDateTime began = t.getStartedAt() != null ? t.getStartedAt() : t.getCalledAt();
        if (began == null) {
            return null;
        }
        return minutesBetween(began, t.getCompletedAt());
    }

    private double averageWait(List<Ticket> tickets) {
        return round1(tickets.stream()
                .map(this::waitMinutes)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0));
    }

    private double averageService(List<Ticket> tickets) {
        return round1(tickets.stream()
                .map(this::serviceMinutes)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0));
    }

    private double minutesBetween(LocalDateTime from, LocalDateTime to) {
        return Duration.between(from, to).toMillis() / 60000.0;
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
