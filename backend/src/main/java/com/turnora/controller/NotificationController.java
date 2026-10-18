package com.turnora.controller;

import com.turnora.dto.TicketDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Student only (checked in SecurityConfig)
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<TicketDto.NotificationResponse> myNotifications(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return notificationService.listForUser(user.id());
    }

    @PutMapping("/{id}/read")
    public TicketDto.NotificationResponse markRead(@PathVariable Long id,
                                                   @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return notificationService.markRead(id, user.id());
    }
}
