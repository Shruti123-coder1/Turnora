package com.turnora.controller;

import com.turnora.dto.AdminDto;
import com.turnora.dto.TicketDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.CounterService;
import com.turnora.service.QueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Counter screen for staff and admin (checked in SecurityConfig), plus the public display board
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;
    private final QueueService queueService;

    // Admin sees all counters, staff sees only own counter
    @GetMapping("/counters")
    public List<AdminDto.CounterResponse> myCounters(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return counterService.listForUser(user);
    }

    // Current token, waiting count and next people of one counter
    @GetMapping("/counters/{id}/state")
    public TicketDto.CounterStateResponse state(@PathVariable Long id,
                                                @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.state(id, user);
    }

    @PostMapping("/counters/{id}/next")
    public TicketDto.CounterStateResponse callNext(@PathVariable Long id,
                                                   @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.callNext(id, user);
    }

    @PutMapping("/counters/{id}/start")
    public TicketDto.CounterStateResponse start(@PathVariable Long id,
                                                @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.start(id, user);
    }

    @PutMapping("/counters/{id}/complete")
    public TicketDto.CounterStateResponse complete(@PathVariable Long id,
                                                   @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.complete(id, user);
    }

    @PutMapping("/counters/{id}/no-show")
    public TicketDto.CounterStateResponse noShow(@PathVariable Long id,
                                                 @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.noShow(id, user);
    }

    @PutMapping("/counters/{id}/transfer")
    public TicketDto.CounterStateResponse transfer(@PathVariable Long id,
                                                   @Valid @RequestBody TicketDto.TransferRequest request,
                                                   @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.transfer(id, request.serviceId(), user);
    }

    @PutMapping("/counters/{id}/status")
    public TicketDto.CounterStateResponse setStatus(@PathVariable Long id,
                                                    @Valid @RequestBody TicketDto.StatusRequest request,
                                                    @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return queueService.setCounterStatus(id, request.status(), user);
    }

    // Public: the display board in the waiting area
    @GetMapping("/board")
    public TicketDto.BoardResponse board() {
        return queueService.board();
    }
}
