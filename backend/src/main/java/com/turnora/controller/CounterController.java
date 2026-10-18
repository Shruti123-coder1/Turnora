package com.turnora.controller;

import com.turnora.dto.AdminDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.CounterService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Staff and admin. Call Next, Start, Complete, No-show and Transfer are added in Phase 4.
@RestController
@RequestMapping("/api/counters")
@RequiredArgsConstructor
public class CounterController {

    private final CounterService counterService;

    @GetMapping
    public List<AdminDto.CounterResponse> myCounters(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return counterService.listForUser(user);
    }
}