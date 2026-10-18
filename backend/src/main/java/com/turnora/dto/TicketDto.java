package com.turnora.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDto {

    // Student (logged in) or guest visitor joins a queue
    public record CreateTicketRequest(
            @NotNull(message = "Select an office")
            Long serviceId,

            // NORMAL, SENIOR_CITIZEN, DIFFERENTLY_ABLED or URGENT (URGENT only by staff). Default NORMAL.
            String priorityType,

            // Name and phone are optional for a logged-in student (account details are used)
            @Size(max = 100, message = "Name is too long")
            String customerName,

            @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
            String phone) {
    }

    public record TicketResponse(
            Long id,
            String tokenNumber,
            Long serviceId,
            String serviceName,
            String customerName,
            String priorityType,
            String status,
            String counterName,
            Integer position,
            Integer peopleAhead,
            Integer estimatedWaitMinutes,
            LocalDateTime createdAt,
            LocalDateTime calledAt,
            LocalDateTime startedAt,
            LocalDateTime completedAt) {
    }

    public record NotificationResponse(
            Long id,
            Long ticketId,
            String tokenNumber,
            String type,
            String channel,
            String message,
            boolean read,
            LocalDateTime createdAt) {
    }

    // ---------- Counter screen (staff) ----------

    public record TransferRequest(
            @NotNull(message = "Select the office to transfer to")
            Long serviceId) {
    }

    public record StatusRequest(
            @NotBlank(message = "Status is required")
            String status) {
    }

    // Everything the staff counter screen needs in one response
    public record CounterStateResponse(
            Long counterId,
            String counterName,
            String counterStatus,
            TicketResponse currentTicket,
            long waitingCount,
            List<TicketResponse> upNext) {
    }

    // ---------- Display board (public) ----------

    public record BoardCounter(
            Long id,
            String name,
            String status,
            String currentToken,
            String ticketStatus,
            String serviceName) {
    }

    public record BoardCall(
            String token,
            String counterName,
            String serviceName,
            LocalDateTime calledAt) {
    }

    public record BoardResponse(
            List<BoardCounter> counters,
            List<BoardCall> recentCalls) {
    }
}
