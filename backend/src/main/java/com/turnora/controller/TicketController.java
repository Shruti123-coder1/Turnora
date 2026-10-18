package com.turnora.controller;

import com.turnora.dto.TicketDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    // Public. A logged-in student, staff or admin sends the JWT, a guest visitor does not.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketDto.TicketResponse create(@Valid @RequestBody TicketDto.CreateTicketRequest request,
                                           @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return ticketService.create(request, user);
    }

    // Student only: own queue history
    @GetMapping("/my")
    public List<TicketDto.TicketResponse> myTickets(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return ticketService.myTickets(user);
    }

    // Public: live status of a token, for example /api/tickets/F-014
    @GetMapping("/{token}")
    public TicketDto.TicketResponse status(@PathVariable String token) {
        return ticketService.status(token);
    }

    // Student only: cancel own waiting token
    @PutMapping("/{id}/cancel")
    public TicketDto.TicketResponse cancel(@PathVariable Long id,
                                           @AuthenticationPrincipal JwtUtil.AuthUser user) {
        return ticketService.cancel(id, user);
    }
}
