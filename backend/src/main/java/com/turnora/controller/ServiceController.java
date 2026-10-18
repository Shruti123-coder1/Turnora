package com.turnora.controller;

import com.turnora.dto.AdminDto;
import com.turnora.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Public: the home page shows these offices with the current queue size
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final AdminService adminService;

    @GetMapping
    public List<AdminDto.ServiceResponse> activeServices() {
        return adminService.listServices(true);
    }
}