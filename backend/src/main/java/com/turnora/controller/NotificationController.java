package com.turnora.controller;

import com.turnora.dto.TicketDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// Each authenticated role can access only its own persisted notifications.
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('STUDENT', 'STAFF', 'ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<TicketDto.NotificationResponse> myNotifications(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return notificationService.listForUser(user.id());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return Map.of("count", notificationService.unreadCount(user.id()));
    }

    @PutMapping("/{id}/read")
    public TicketDto.NotificationResponse markRead(@PathVariable Long id,
                                                   @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return notificationService.markRead(id, user.id());
    }

    @PutMapping("/read-all")
    public Map<String, Long> markAllRead(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return Map.of("updated", notificationService.markAllRead(user.id()));
    }
}
