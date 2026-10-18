package com.turnora.controller;

import com.turnora.dto.TicketDto;
import com.turnora.exception.AppException;
import com.turnora.exception.GlobalExceptionHandler;
import com.turnora.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// API tests for the ticket endpoints (no database, no login needed)
class TicketControllerTest {

    private MockMvc mockMvc;
    private TicketService ticketService;

    @BeforeEach
    void setUp() {
        ticketService = Mockito.mock(TicketService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new TicketController(ticketService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private TicketDto.TicketResponse sample(String token, String status, Integer position) {
        return new TicketDto.TicketResponse(
                1L, token, 1L, "Accounts Section (Fee Payment)", "Amit", "NORMAL", status,
                null, position, position == null ? null : position - 1, 5, null, null, null, null);
    }

    @Test
    void createToken_returns201AndTokenNumber() throws Exception {
        when(ticketService.create(any(), any())).thenReturn(sample("F-001", "WAITING", 1));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceId\":1,\"customerName\":\"Amit\",\"phone\":\"9876543210\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenNumber").value("F-001"))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void createToken_withoutOffice_returns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerName\":\"Amit\",\"phone\":\"9876543210\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Select an office"));
    }

    @Test
    void createToken_withWrongPhone_returns400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"serviceId\":1,\"customerName\":\"Amit\",\"phone\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Phone number must be 10 digits"));
    }

    @Test
    void trackToken_returnsLiveStatus() throws Exception {
        when(ticketService.status("F-001")).thenReturn(sample("F-001", "WAITING", 2));

        mockMvc.perform(get("/api/tickets/F-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenNumber").value("F-001"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.position").value(2));
    }

    @Test
    void trackToken_unknownToken_returns404() throws Exception {
        when(ticketService.status("F-999")).thenThrow(AppException.notFound("Token not found for today"));

        mockMvc.perform(get("/api/tickets/F-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Token not found for today"));
    }
}
