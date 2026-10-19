package com.turnora.service;

import com.turnora.dto.TicketDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.Notification;
import com.turnora.model.entity.Ticket;
import com.turnora.model.entity.User;
import com.turnora.model.enums.Role;
import com.turnora.repository.NotificationRepository;
import com.turnora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

// In-app notifications (saved + pushed live by WebSocket) and SMS notifications
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final SimpMessagingTemplate messagingTemplate;

    // Runs the action after the database transaction is saved, so screens never see half-saved data.
    // If the action fails, the queue is not affected.
    public void runAfterCommit(Runnable action) {
        Runnable safe = () -> {
            try {
                action.run();
            } catch (Exception e) {
                log.warn("Live update failed: {}", e.getMessage());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safe.run();
                }
            });
        } else {
            safe.run();
        }
    }

    // Saves an in-app notification. If sendSms is true, an SMS is saved and sent too.
    @Transactional
    public void notifyTicket(Ticket ticket, String type, String message, boolean sendSms) {
        User user = ticket.getUser();

        if (user != null) {
            notifyUser(user, ticket, type, message);
        }

        if (sendSms) {
            Notification sms = notificationRepository.save(Notification.builder()
                    .user(user)
                    .ticket(ticket)
                    .type(type)
                    .channel("SMS")
                    .phone(ticket.getPhone())
                    .message(message)
                    .build());
            Long smsId = sms.getId();
            String phone = ticket.getPhone();
            runAfterCommit(() -> smsService.send(smsId, phone, message));
        }
    }

    // Role audiences are expanded into one private, persisted notification per active user.
    // This keeps reads and real-time updates scoped to the intended recipient.
    @Transactional
    public void notifyRole(Role role, Ticket ticket, String type, String message) {
        userRepository.findByRoleAndActiveTrueOrderByIdAsc(role)
                .forEach(user -> notifyUser(user, ticket, type, message));
    }

    @Transactional
    public void notifyOperations(Ticket ticket, String type, String message) {
        notifyRole(Role.STAFF, ticket, type, message);
        notifyRole(Role.ADMIN, ticket, type, message);
    }

    private void notifyUser(User user, Ticket ticket, String type, String message) {
        Notification inApp = notificationRepository.save(Notification.builder()
                .user(user)
                .ticket(ticket)
                .type(type)
                .channel("IN_APP")
                .phone(ticket != null ? ticket.getPhone() : null)
                .message(message)
                .build());
        TicketDto.NotificationResponse payload = toResponse(inApp);
        Long userId = user.getId();
        runAfterCommit(() -> messagingTemplate.convertAndSend("/topic/user/" + userId, payload));
    }

    @Transactional(readOnly = true)
    public List<TicketDto.NotificationResponse> listForUser(Long userId) {
        return notificationRepository.findTop50ByUserIdAndChannelOrderByCreatedAtDesc(userId, "IN_APP").stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TicketDto.NotificationResponse markRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> AppException.notFound("Notification not found"));
        if (n.getUser() == null || !n.getUser().getId().equals(userId)) {
            throw AppException.notFound("Notification not found");
        }
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndChannelAndReadFalse(userId, "IN_APP");
    }

    @Transactional
    public long markAllRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndChannelAndReadFalse(userId, "IN_APP");
        unread.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unread);
        return unread.size();
    }

    // Must be called inside a transaction (the ticket is loaded lazily)
    private TicketDto.NotificationResponse toResponse(Notification n) {
        return new TicketDto.NotificationResponse(
                n.getId(),
                n.getTicket() != null ? n.getTicket().getId() : null,
                n.getTicket() != null ? n.getTicket().getTokenNumber() : null,
                n.getType(),
                n.getChannel(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt());
    }
}
