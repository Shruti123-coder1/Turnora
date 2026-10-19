package com.turnora.service;

import com.turnora.dto.TicketDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.ServiceType;
import com.turnora.model.entity.Ticket;
import com.turnora.model.entity.User;
import com.turnora.model.enums.PriorityType;
import com.turnora.model.enums.Role;
import com.turnora.model.enums.TicketStatus;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.repository.UserRepository;
import com.turnora.security.JwtUtil;
import com.turnora.util.TokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Creating tokens, live status, cancel and "my tokens"
@Service
@RequiredArgsConstructor
public class TicketService {

    private static final List<TicketStatus> LIVE =
            List.of(TicketStatus.WAITING, TicketStatus.CALLED, TicketStatus.SERVING);

    private final TicketRepository ticketRepository;
    private final ServiceTypeRepository serviceRepository;
    private final UserRepository userRepository;
    private final TokenGenerator tokenGenerator;
    private final QueueService queueService;
    private final NotificationService notificationService;

    // actor is null for a guest visitor
    @Transactional
    public TicketDto.TicketResponse create(TicketDto.CreateTicketRequest req, JwtUtil.AuthUser actor) {
        // Lock the office, so token numbers are never repeated
        ServiceType service = serviceRepository.findByIdForUpdate(req.serviceId())
                .orElseThrow(() -> AppException.notFound("Office not found"));
        if (!service.isActive()) {
            throw AppException.badRequest("This office is closed right now");
        }

        boolean byStaff = actor != null && (actor.role() == Role.STAFF || actor.role() == Role.ADMIN);
        User user = null;
        if (actor != null && actor.role() == Role.STUDENT) {
            user = userRepository.findById(actor.id()).orElse(null);
        }

        String name = firstNonBlank(req.customerName(), user != null ? user.getFullName() : null);
        String phone = firstNonBlank(req.phone(), user != null ? user.getPhone() : null);
        if (name == null) {
            throw AppException.badRequest("Name is required");
        }
        if (phone == null || !phone.trim().matches("^[0-9]{10}$")) {
            throw AppException.badRequest("A valid 10 digit phone number is required");
        }
        name = name.trim();
        phone = phone.trim();

        PriorityType priority = parsePriority(req.priorityType());
        if (priority == PriorityType.URGENT && !byStaff) {
            throw AppException.forbidden("Urgent priority must be approved by staff at the counter");
        }

        LocalDate today = LocalDate.now();
        if (ticketRepository.existsByServiceIdAndQueueDateAndPhoneAndStatusIn(service.getId(), today, phone, LIVE)) {
            throw AppException.conflict("You already have an active token for this office");
        }

        int sequence = ticketRepository.maxSequence(today, service.getPrefix() + "-%") + 1;

        Ticket ticket = Ticket.builder()
                .tokenNumber(tokenGenerator.format(service.getPrefix(), sequence))
                .sequenceNo(sequence)
                .queueDate(today)
                .service(service)
                .user(user)
                .customerName(name)
                .phone(phone)
                .priorityType(priority)
                .priorityWeight(priority.getWeight())
                .status(TicketStatus.WAITING)
                .source(byStaff ? "STAFF" : "QR")
                .createdAt(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .build();
        ticket = ticketRepository.save(ticket);

        queueService.recordEvent(ticket, "CREATED", null, TicketStatus.WAITING, null,
                actor != null ? actor.id() : null);

        TicketDto.TicketResponse response = queueService.statusFor(ticket, true);

        String message = "Your token " + ticket.getTokenNumber() + " for " + service.getName()
                + " is confirmed. Estimated wait: " + response.estimatedWaitMinutes() + " min.";
        notificationService.notifyTicket(ticket, "CREATED", message, true);
        notificationService.notifyOperations(ticket, "QUEUE_CREATED",
                "New token " + ticket.getTokenNumber() + " joined the " + service.getName() + " queue.");

        Long serviceId = service.getId();
        Long ticketId = ticket.getId();
        notificationService.runAfterCommit(() -> queueService.broadcast(serviceId, ticketId));

        return response;
    }

    // Public live status by token, for example F-014 (today's tokens only)
    @Transactional(readOnly = true)
    public TicketDto.TicketResponse status(String token) {
        Ticket ticket = ticketRepository
                .findByTokenNumberAndQueueDate(token.trim().toUpperCase(), LocalDate.now())
                .orElseThrow(() -> AppException.notFound("Token not found for today"));
        return queueService.statusFor(ticket, false);
    }

    // A student cancels his or her own waiting token
    @Transactional
    public TicketDto.TicketResponse cancel(Long ticketId, JwtUtil.AuthUser actor) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> AppException.notFound("Token not found"));

        if (ticket.getUser() == null || !ticket.getUser().getId().equals(actor.id())) {
            throw AppException.notFound("Token not found");
        }
        if (ticket.getStatus() != TicketStatus.WAITING) {
            throw AppException.conflict("Only a waiting token can be cancelled");
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        ticket = ticketRepository.save(ticket);
        queueService.recordEvent(ticket, "CANCELLED", TicketStatus.WAITING, TicketStatus.CANCELLED,
                null, actor.id());
        notificationService.notifyTicket(ticket, "CANCELLED",
                "Your token " + ticket.getTokenNumber() + " has been cancelled.", false);
        notificationService.notifyOperations(ticket, "QUEUE_CANCELLED",
                "Token " + ticket.getTokenNumber() + " was cancelled from the "
                        + ticket.getService().getName() + " queue.");

        Long serviceId = ticket.getService().getId();
        Long id = ticket.getId();
        queueService.notifyNearTurn(serviceId);
        notificationService.runAfterCommit(() -> queueService.broadcast(serviceId, id));

        return queueService.statusFor(ticket, true);
    }

    // Queue history of the logged-in student
    @Transactional(readOnly = true)
    public List<TicketDto.TicketResponse> myTickets(JwtUtil.AuthUser actor) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(actor.id()).stream()
                .map(t -> queueService.statusFor(t, true))
                .toList();
    }

    private PriorityType parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return PriorityType.NORMAL;
        }
        try {
            return PriorityType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Priority must be NORMAL, SENIOR_CITIZEN, DIFFERENTLY_ABLED or URGENT");
        }
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
