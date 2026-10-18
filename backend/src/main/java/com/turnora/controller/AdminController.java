package com.turnora.controller;

import com.turnora.dto.AdminDto;
import com.turnora.service.AdminService;
import com.turnora.service.AnalyticsService;
import com.turnora.service.CounterService;
import com.turnora.service.HistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Admin only (checked in SecurityConfig)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CounterService counterService;
    private final AnalyticsService analyticsService;
    private final HistoryService historyService;

    // ---------- Offices ----------
    @GetMapping("/services")
    public List<AdminDto.ServiceResponse> services() {
        return adminService.listServices(false);
    }

    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDto.ServiceResponse createService(@Valid @RequestBody AdminDto.ServiceRequest request) {
        return adminService.createService(request);
    }

    @PutMapping("/services/{id}")
    public AdminDto.ServiceResponse updateService(@PathVariable Long id,
                                                  @Valid @RequestBody AdminDto.ServiceRequest request) {
        return adminService.updateService(id, request);
    }

    // ---------- Counters ----------
    @GetMapping("/counters")
    public List<AdminDto.CounterResponse> counters() {
        return counterService.listAll();
    }

    @PostMapping("/counters")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDto.CounterResponse createCounter(@Valid @RequestBody AdminDto.CounterRequest request) {
        return counterService.create(request);
    }

    @PutMapping("/counters/{id}")
    public AdminDto.CounterResponse updateCounter(@PathVariable Long id,
                                                  @Valid @RequestBody AdminDto.CounterRequest request) {
        return counterService.update(id, request);
    }

    @DeleteMapping("/counters/{id}")
    public Map<String, String> deleteCounter(@PathVariable Long id) {
        counterService.delete(id);
        return Map.of("message", "Counter removed");
    }

    // ---------- Staff ----------
    @GetMapping("/users")
    public List<AdminDto.StaffResponse> staff() {
        return adminService.listStaff();
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminDto.StaffResponse createStaff(@Valid @RequestBody AdminDto.StaffRequest request) {
        return adminService.createStaff(request);
    }

    @PutMapping("/users/{id}/active")
    public AdminDto.StaffResponse setStaffActive(@PathVariable Long id,
                                                 @RequestBody AdminDto.ActiveRequest request) {
        return adminService.setStaffActive(id, request.active());
    }

    // ---------- Analytics (dashboard) ----------
    // Example: /api/admin/analytics or /api/admin/analytics?date=2026-10-18
    @GetMapping("/analytics")
    public AdminDto.AnalyticsResponse analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return analyticsService.analytics(date);
    }

    // ---------- Queue history ----------
    // Example: /api/admin/history?from=2026-10-01&to=2026-10-18&serviceId=1&status=COMPLETED&q=F-0&page=0&size=20
    @GetMapping("/history")
    public AdminDto.HistoryPage history(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long serviceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "q") String text,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return historyService.search(from, to, serviceId, status, text, page, size);
    }

    // Every status change of one token
    @GetMapping("/history/{ticketId}/events")
    public List<AdminDto.EventRow> events(@PathVariable Long ticketId) {
        return historyService.events(ticketId);
    }
}
