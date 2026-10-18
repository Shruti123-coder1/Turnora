package com.turnora.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDto {

    // ---------- Offices (services) ----------
    public record ServiceRequest(
            @NotBlank(message = "Office name is required")
            @Size(max = 100, message = "Office name is too long")
            String name,

            @NotBlank(message = "Token prefix is required")
            @Pattern(regexp = "^[A-Za-z0-9]{1,5}$", message = "Prefix must be 1 to 5 letters or digits")
            String prefix,

            @Size(max = 255, message = "Description is too long")
            String description,

            @DecimalMin(value = "1", message = "Average service minutes must be at least 1")
            @DecimalMax(value = "120", message = "Average service minutes must be 120 or less")
            Double avgServiceMinutes,

            Boolean active) {
    }

    public record ServiceResponse(
            Long id,
            String name,
            String prefix,
            String description,
            double avgServiceMinutes,
            boolean active,
            long waitingCount) {
    }

    // ---------- Counters ----------
    public record CounterRequest(
            @NotBlank(message = "Counter name is required")
            @Size(max = 50, message = "Counter name is too long")
            String name,

            // ACTIVE, PAUSED or OFFLINE (default ACTIVE)
            String status,

            Long staffId,

            List<Long> serviceIds) {
    }

    public record CounterResponse(
            Long id,
            String name,
            String status,
            Long staffId,
            String staffName,
            List<Long> serviceIds,
            List<String> serviceNames,
            String currentToken) {
    }

    // ---------- Staff ----------
    public record StaffRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name is too long")
            String fullName,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email")
            String email,

            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
            String phone,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
            String password) {
    }

    public record StaffResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            boolean active,
            Long counterId,
            String counterName) {
    }

    public record ActiveRequest(boolean active) {
    }

    // ---------- Analytics (admin dashboard) ----------
    public record HourCount(int hour, long tokens) {
    }

    public record OfficeStat(
            Long serviceId,
            String name,
            String prefix,
            long tickets,
            long completed,
            double avgWaitMinutes,
            double avgServiceMinutes) {
    }

    public record CounterStat(
            Long counterId,
            String name,
            long served,
            double avgServiceMinutes) {
    }

    public record AnalyticsResponse(
            LocalDate date,
            long totalTokens,
            long waiting,
            long inProgress,
            long completed,
            long noShow,
            long cancelled,
            double avgWaitMinutes,
            double avgServiceMinutes,
            List<HourCount> tokensByHour,
            List<OfficeStat> offices,
            List<CounterStat> counters) {
    }

    // ---------- Queue history (admin) ----------
    public record HistoryRow(
            Long id,
            String tokenNumber,
            String serviceName,
            String customerName,
            String phone,
            String priorityType,
            String status,
            String counterName,
            String source,
            LocalDate queueDate,
            LocalDateTime createdAt,
            LocalDateTime calledAt,
            LocalDateTime completedAt,
            Double waitMinutes,
            Double serviceMinutes) {
    }

    public record HistoryPage(
            List<HistoryRow> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
    }

    public record EventRow(
            Long id,
            String eventType,
            String fromStatus,
            String toStatus,
            String counterName,
            String actorName,
            LocalDateTime createdAt) {
    }
}
