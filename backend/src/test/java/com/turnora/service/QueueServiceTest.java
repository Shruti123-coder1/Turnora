package com.turnora.service;

import com.turnora.model.entity.ServiceType;
import com.turnora.model.enums.CounterStatus;
import com.turnora.model.enums.PriorityType;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.util.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Queue rules: waiting time, moving average, token numbers and priority weights
@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private CounterRepository counterRepository;

    @Mock
    private ServiceTypeRepository serviceRepository;

    private EtaService etaService;
    private final TokenGenerator tokenGenerator = new TokenGenerator();

    @BeforeEach
    void setUp() {
        etaService = new EtaService(counterRepository, serviceRepository);
    }

    private ServiceType office(double averageMinutes) {
        return ServiceType.builder()
                .id(1L)
                .name("Accounts Section")
                .prefix("F")
                .avgServiceMinutes(averageMinutes)
                .build();
    }

    @Test
    void waitingTime_isPeopleAheadTimesAverageDividedByActiveCounters() {
        when(counterRepository.countByServiceAndStatus(1L, CounterStatus.ACTIVE)).thenReturn(2L);

        // 4 people x 6 minutes / 2 counters = 12 minutes
        assertEquals(12, etaService.estimateMinutes(office(6), 4));
    }

    @Test
    void waitingTime_isRoundedUp() {
        when(counterRepository.countByServiceAndStatus(1L, CounterStatus.ACTIVE)).thenReturn(2L);

        // 1 person x 5 minutes / 2 counters = 2.5, shown as 3
        assertEquals(3, etaService.estimateMinutes(office(5), 1));
    }

    @Test
    void waitingTime_usesAtLeastOneCounter() {
        when(counterRepository.countByServiceAndStatus(1L, CounterStatus.ACTIVE)).thenReturn(0L);

        // No active counter is treated as 1 counter: 3 x 5 / 1 = 15
        assertEquals(15, etaService.estimateMinutes(office(5), 3));
    }

    @Test
    void waitingTime_isZeroWhenNobodyIsAhead() {
        assertEquals(0, etaService.estimateMinutes(office(5), 0));
    }

    @Test
    void averageServiceTime_isUpdatedWithMovingAverage() {
        ServiceType service = office(10);

        etaService.updateAverage(service, 20);

        // new average = 0.7 x 10 + 0.3 x 20 = 13
        assertEquals(13.0, service.getAvgServiceMinutes(), 0.001);
        verify(serviceRepository).save(service);
    }

    @Test
    void tokenNumber_hasOfficePrefixAndThreeDigits() {
        assertEquals("F-014", tokenGenerator.format("F", 14));
        assertEquals("E-001", tokenGenerator.format("E", 1));
        assertEquals("L-1000", tokenGenerator.format("L", 1000));
    }

    @Test
    void priorityWeights_urgentFirstThenSeniorAndDifferentlyAbledThenNormal() {
        assertTrue(PriorityType.URGENT.getWeight() > PriorityType.SENIOR_CITIZEN.getWeight());
        assertEquals(PriorityType.SENIOR_CITIZEN.getWeight(), PriorityType.DIFFERENTLY_ABLED.getWeight());
        assertTrue(PriorityType.SENIOR_CITIZEN.getWeight() > PriorityType.NORMAL.getWeight());
        assertEquals(1, PriorityType.NORMAL.getWeight());
    }
}
