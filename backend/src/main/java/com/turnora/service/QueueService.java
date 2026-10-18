package com.turnora.service;

import com.turnora.dto.TicketDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.Counter;
import com.turnora.model.entity.ServiceType;
import com.turnora.model.entity.Ticket;
import com.turnora.model.entity.TicketEvent;
import com.turnora.model.enums.CounterStatus;
import com.turnora.model.enums.Role;
import com.turnora.model.enums.TicketStatus;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.NotificationRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketEventRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// The heart of Turnora: the queue.
// Call Next (with locking), Start, Complete, No-show, Transfer, live updates and history events.
@Service
@RequiredArgsConstructor
public class QueueService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final CounterRepository counterRepository;
    private final ServiceTypeRepository serviceRepository;
    private final NotificationRepository notificationRepository;
    private final EtaService etaService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlatformTransactionManager transactionManager;

    // =====================================================================
    //  Counter actions (staff and admin)
    // =====================================================================

    @Transactional(readOnly = true)
    public TicketDto.CounterStateResponse state(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = counterRepository.findById(counterId)
                .orElseThrow(() -> AppException.notFound("Counter not found"));
        checkAccess(counter, actor);
        return buildState(counter);
    }

    // Call Next: the counter takes the next person (highest priority first, then first come first served)
    @Transactional
    public TicketDto.CounterStateResponse callNext(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);

        if (counter.getStatus() != CounterStatus.ACTIVE) {
            throw AppException.conflict("This counter is " + counter.getStatus() + ". Set it to ACTIVE first.");
        }
        if (counter.getCurrentTicket() != null) {
            throw AppException.conflict("Finish the current token first (Complete or No-show)");
        }

        List<Long> serviceIds = counter.getServices().stream().map(ServiceType::getId).toList();
        if (serviceIds.isEmpty()) {
            throw AppException.conflict("No office is linked to this counter");
        }

        // The row lock makes sure two counters never get the same person
        Ticket next = null;
        for (int attempt = 0; attempt < 3 && next == null; attempt++) {
            List<Ticket> found = ticketRepository.findNextForUpdate(
                    serviceIds, LocalDate.now(), TicketStatus.WAITING, PageRequest.of(0, 1));
            if (!found.isEmpty()) {
                next = found.get(0);
            }
        }
        if (next == null) {
            throw AppException.notFound("No one is waiting right now");
        }

        next.setStatus(TicketStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        next.setCounter(counter);
        ticketRepository.save(next);

        counter.setCurrentTicket(next);
        counterRepository.save(counter);

        recordEvent(next, "CALLED", TicketStatus.WAITING, TicketStatus.CALLED, counter.getId(), actor.id());
        notificationService.notifyTicket(next, "CALLED",
                "Token " + next.getTokenNumber() + ", please go to " + counter.getName() + ".", true);

        afterChange(next.getService().getId(), next.getId());
        return buildState(counter);
    }

    // The person has reached the counter and service starts
    @Transactional
    public TicketDto.CounterStateResponse start(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);
        Ticket ticket = requireCurrent(counter);

        if (ticket.getStatus() != TicketStatus.CALLED) {
            throw AppException.conflict("This token is already being served");
        }

        ticket.setStatus(TicketStatus.SERVING);
        ticket.setStartedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        recordEvent(ticket, "STARTED", TicketStatus.CALLED, TicketStatus.SERVING, counter.getId(), actor.id());
        afterChange(ticket.getService().getId(), ticket.getId());
        return buildState(counter);
    }

    // Service is finished. The average service time of the office is updated.
    @Transactional
    public TicketDto.CounterStateResponse complete(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);
        Ticket ticket = requireCurrent(counter);
        TicketStatus from = ticket.getStatus();

        LocalDateTime now = LocalDateTime.now();
        ticket.setStatus(TicketStatus.COMPLETED);
        ticket.setCompletedAt(now);
        ticketRepository.save(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        LocalDateTime began = ticket.getStartedAt() != null ? ticket.getStartedAt() : ticket.getCalledAt();
        if (began != null) {
            double minutes = Duration.between(began, now).toSeconds() / 60.0;
            etaService.updateAverage(ticket.getService(), minutes);
        }

        recordEvent(ticket, "COMPLETED", from, TicketStatus.COMPLETED, counter.getId(), actor.id());
        afterChange(ticket.getService().getId(), ticket.getId());
        return buildState(counter);
    }

    // The person did not come to the counter
    @Transactional
    public TicketDto.CounterStateResponse noShow(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);
        Ticket ticket = requireCurrent(counter);
        TicketStatus from = ticket.getStatus();

        ticket.setStatus(TicketStatus.NO_SHOW);
        ticket.setCompletedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        recordEvent(ticket, "NO_SHOW", from, TicketStatus.NO_SHOW, counter.getId(), actor.id());
        notificationService.notifyTicket(ticket, "NO_SHOW",
                "You missed your turn. Token " + ticket.getTokenNumber() + " is closed.", false);

        afterChange(ticket.getService().getId(), ticket.getId());
        return buildState(counter);
    }

    // Moves the current person to the waiting line of another office. The token number stays the same.
    @Transactional
    public TicketDto.CounterStateResponse transfer(Long counterId, Long targetServiceId, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);
        Ticket ticket = requireCurrent(counter);

        ServiceType target = serviceRepository.findById(targetServiceId)
                .orElseThrow(() -> AppException.notFound("Office not found"));
        if (!target.isActive()) {
            throw AppException.badRequest("That office is closed right now");
        }
        ServiceType old = ticket.getService();
        if (old.getId().equals(target.getId())) {
            throw AppException.badRequest("The token is already in this office queue");
        }

        TicketStatus from = ticket.getStatus();
        ticket.setService(target);
        ticket.setStatus(TicketStatus.WAITING);
        ticket.setCounter(null);
        ticket.setCalledAt(null);
        ticket.setStartedAt(null);
        ticketRepository.save(ticket);

        counter.setCurrentTicket(null);
        counterRepository.save(counter);

        recordEvent(ticket, "TRANSFERRED", from, TicketStatus.WAITING, counter.getId(), actor.id());
        notificationService.notifyTicket(ticket, "TRANSFERRED",
                "You are moved to the " + target.getName() + " queue. Your token " + ticket.getTokenNumber()
                        + " stays the same.", false);

        notifyNearTurn(old.getId());
        notifyNearTurn(target.getId());

        Long oldId = old.getId();
        Long targetId = target.getId();
        Long ticketId = ticket.getId();
        notificationService.runAfterCommit(() -> {
            broadcast(oldId, null);
            broadcast(targetId, ticketId);
        });
        return buildState(counter);
    }

    // Staff or admin can pause a counter or set it ACTIVE or OFFLINE
    @Transactional
    public TicketDto.CounterStateResponse setCounterStatus(Long counterId, String status, JwtUtil.AuthUser actor) {
        Counter counter = lockCounter(counterId, actor);

        CounterStatus newStatus;
        try {
            newStatus = CounterStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Status must be ACTIVE, PAUSED or OFFLINE");
        }
        if (newStatus != CounterStatus.ACTIVE && counter.getCurrentTicket() != null) {
            throw AppException.conflict("Finish the current token first (Complete or No-show)");
        }

        counter.setStatus(newStatus);
        counterRepository.save(counter);

        // Waiting time depends on the number of active counters, so the queues are refreshed
        List<Long> serviceIds = counter.getServices().stream().map(ServiceType::getId).toList();
        notificationService.runAfterCommit(() -> {
            for (Long id : serviceIds) {
                broadcast(id, null);
            }
            messagingTemplate.convertAndSend("/topic/board", Map.of("event", "COUNTER_CHANGED"));
        });
        return buildState(counter);
    }

    // =====================================================================
    //  Display board (public)
    // =====================================================================

    @Transactional(readOnly = true)
    public TicketDto.BoardResponse board() {
        List<TicketDto.BoardCounter> counters = counterRepository.findAllByOrderByIdAsc().stream()
                .map(c -> {
                    Ticket t = c.getCurrentTicket();
                    return new TicketDto.BoardCounter(
                            c.getId(),
                            c.getName(),
                            c.getStatus().name(),
                            t != null ? t.getTokenNumber() : null,
                            t != null ? t.getStatus().name() : null,
                            t != null ? t.getService().getName() : null);
                })
                .toList();

        List<TicketDto.BoardCall> calls = ticketRepository
                .findByQueueDateAndCalledAtIsNotNullOrderByCalledAtDesc(LocalDate.now(), PageRequest.of(0, 8))
                .stream()
                .filter(t -> t.getCounter() != null)
                .map(t -> new TicketDto.BoardCall(
                        t.getTokenNumber(),
                        t.getCounter().getName(),
                        t.getService().getName(),
                        t.getCalledAt()))
                .toList();

        return new TicketDto.BoardResponse(counters, calls);
    }

    // =====================================================================
    //  Position, waiting time, live updates, history
    // =====================================================================

    // Position and waiting time of one ticket
    @Transactional(readOnly = true)
    public TicketDto.TicketResponse statusFor(Ticket t, boolean includeName) {
        Integer position = null;
        Integer ahead = null;
        Integer eta = null;

        if (t.getStatus() == TicketStatus.WAITING) {
            long count = ticketRepository.countAhead(
                    t.getService().getId(), t.getQueueDate(), TicketStatus.WAITING,
                    t.getPriorityWeight(), t.getCreatedAt(), t.getId());
            ahead = (int) count;
            position = ahead + 1;
            eta = etaService.estimateMinutes(t.getService(), count);
        } else if (t.getStatus() == TicketStatus.CALLED || t.getStatus() == TicketStatus.SERVING) {
            eta = 0;
        }

        return toResponse(t, includeName, position, ahead, eta);
    }

    // Sends "your turn is near" (in-app + SMS) to the person who has 2 people ahead. Sent only once per ticket.
    @Transactional
    public void notifyNearTurn(Long serviceId) {
        List<Ticket> queue = ticketRepository.findQueue(serviceId, LocalDate.now(), TicketStatus.WAITING);
        if (queue.size() < 3) {
            return;
        }
        Ticket third = queue.get(2);
        if (notificationRepository.existsByTicketIdAndType(third.getId(), "NEAR")) {
            return;
        }
        notificationService.notifyTicket(third, "NEAR",
                "Your turn is near. Please come to the " + third.getService().getName() + ".", true);
    }

    // Pushes the new position and waiting time to every waiting person of this office,
    // and tells the display board and home page that the queue changed.
    // Runs in its own read-only transaction, after the main change is saved.
    public void broadcast(Long serviceId, Long changedTicketId) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(true);
        template.executeWithoutResult(status -> doBroadcast(serviceId, changedTicketId));
    }

    private void doBroadcast(Long serviceId, Long changedTicketId) {
        List<Ticket> waiting = ticketRepository.findQueue(serviceId, LocalDate.now(), TicketStatus.WAITING);

        if (!waiting.isEmpty()) {
            long counters = etaService.activeCounters(waiting.get(0).getService());
            for (int i = 0; i < waiting.size(); i++) {
                Ticket t = waiting.get(i);
                int eta = etaService.estimateMinutes(t.getService(), i, counters);
                push(toResponse(t, false, i + 1, i, eta));
            }
        }

        // The ticket that just changed (called, cancelled, completed ...) gets its own update
        if (changedTicketId != null) {
            ticketRepository.findById(changedTicketId).ifPresent(t -> {
                if (t.getStatus() != TicketStatus.WAITING) {
                    push(statusFor(t, false));
                }
            });
        }

        messagingTemplate.convertAndSend("/topic/board",
                Map.of("event", "QUEUE_CHANGED", "serviceId", serviceId));
    }

    // Saves one line of queue history (every status change is saved)
    @Transactional
    public void recordEvent(Ticket ticket, String eventType, TicketStatus from, TicketStatus to,
                            Long counterId, Long actorId) {
        ticketEventRepository.save(TicketEvent.builder()
                .ticket(ticket)
                .eventType(eventType)
                .fromStatus(from)
                .toStatus(to)
                .counterId(counterId)
                .actorId(actorId)
                .build());
    }

    // =====================================================================
    //  Helpers
    // =====================================================================

    // After every change: "turn is near" alert, then live updates once the data is saved
    private void afterChange(Long serviceId, Long ticketId) {
        notifyNearTurn(serviceId);
        notificationService.runAfterCommit(() -> broadcast(serviceId, ticketId));
    }

    private Counter lockCounter(Long counterId, JwtUtil.AuthUser actor) {
        Counter counter = counterRepository.findByIdForUpdate(counterId)
                .orElseThrow(() -> AppException.notFound("Counter not found"));
        checkAccess(counter, actor);
        return counter;
    }

    // Admin can use every counter. Staff can use only the counter assigned to them.
    private void checkAccess(Counter counter, JwtUtil.AuthUser actor) {
        if (actor.role() == Role.ADMIN) {
            return;
        }
        if (counter.getStaff() == null || !counter.getStaff().getId().equals(actor.id())) {
            throw AppException.forbidden("This is not your counter");
        }
    }

    private Ticket requireCurrent(Counter counter) {
        Ticket ticket = counter.getCurrentTicket();
        if (ticket == null) {
            throw AppException.conflict("This counter has no token right now. Press Call Next.");
        }
        return ticket;
    }

    // Everything the counter screen needs: current token, waiting count and the next few people
    private TicketDto.CounterStateResponse buildState(Counter counter) {
        Ticket current = counter.getCurrentTicket();
        TicketDto.TicketResponse currentResponse = current != null ? statusFor(current, true) : null;

        LocalDate today = LocalDate.now();
        List<Ticket> merged = new ArrayList<>();
        long waiting = 0;
        for (ServiceType s : counter.getServices()) {
            List<Ticket> queue = ticketRepository.findQueue(s.getId(), today, TicketStatus.WAITING);
            waiting += queue.size();
            merged.addAll(queue);
        }
        merged.sort(Comparator.comparingInt(Ticket::getPriorityWeight).reversed()
                .thenComparing(Ticket::getCreatedAt)
                .thenComparing(Ticket::getId));

        List<TicketDto.TicketResponse> upNext = merged.stream()
                .limit(5)
                .map(t -> statusFor(t, true))
                .toList();

        return new TicketDto.CounterStateResponse(
                counter.getId(), counter.getName(), counter.getStatus().name(),
                currentResponse, waiting, upNext);
    }

    private void push(TicketDto.TicketResponse response) {
        messagingTemplate.convertAndSend("/topic/ticket/" + response.tokenNumber(), response);
    }

    // Must be called inside a transaction (office and counter are loaded lazily)
    private TicketDto.TicketResponse toResponse(Ticket t, boolean includeName,
                                                Integer position, Integer ahead, Integer eta) {
        return new TicketDto.TicketResponse(
                t.getId(),
                t.getTokenNumber(),
                t.getService().getId(),
                t.getService().getName(),
                includeName ? t.getCustomerName() : null,
                t.getPriorityType().name(),
                t.getStatus().name(),
                t.getCounter() != null ? t.getCounter().getName() : null,
                position,
                ahead,
                eta,
                t.getCreatedAt(),
                t.getCalledAt(),
                t.getStartedAt(),
                t.getCompletedAt());
    }
}
