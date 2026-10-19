package com.turnora.service;

import com.turnora.dto.AdminDto;
import com.turnora.exception.AppException;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CounterServiceTest {

    @Mock private CounterRepository counterRepository;
    @Mock private ServiceTypeRepository serviceRepository;
    @Mock private UserRepository userRepository;
    @Mock private TicketRepository ticketRepository;

    private CounterService counterService;

    @BeforeEach
    void setUp() {
        counterService = new CounterService(counterRepository, serviceRepository, userRepository, ticketRepository);
    }

    @Test
    void create_rejectsAnExistingCounterNameIgnoringCase() {
        when(counterRepository.existsByNameIgnoreCase("main counter")).thenReturn(true);

        AppException error = assertThrows(AppException.class, () -> counterService.create(
                new AdminDto.CounterRequest("main counter", "ACTIVE", null, List.of(1L))));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals("A counter with this name already exists", error.getMessage());
    }
}
