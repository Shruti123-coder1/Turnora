package com.turnora.service;

import com.turnora.model.entity.ServiceType;
import com.turnora.model.enums.CounterStatus;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Estimated waiting time = people ahead x average service minutes / active counters
@Service
@RequiredArgsConstructor
public class EtaService {

    private final CounterRepository counterRepository;
    private final ServiceTypeRepository serviceRepository;

    // ACTIVE counters that serve this office (at least 1)
    public long activeCounters(ServiceType service) {
        long count = counterRepository.countByServiceAndStatus(service.getId(), CounterStatus.ACTIVE);
        return Math.max(1, count);
    }

    public int estimateMinutes(ServiceType service, long peopleAhead) {
        return estimateMinutes(service, peopleAhead, activeCounters(service));
    }

    public int estimateMinutes(ServiceType service, long peopleAhead, long activeCounters) {
        double minutes = peopleAhead * service.getAvgServiceMinutes() / Math.max(1, activeCounters);
        return (int) Math.ceil(minutes);
    }

    // Called after a ticket is completed: new average = 0.7 x old average + 0.3 x actual minutes
    @Transactional
    public void updateAverage(ServiceType service, double actualMinutes) {
        double actual = Math.max(0.5, Math.min(actualMinutes, 120));
        double updated = 0.7 * service.getAvgServiceMinutes() + 0.3 * actual;
        service.setAvgServiceMinutes(Math.round(updated * 100.0) / 100.0);
        serviceRepository.save(service);
    }
}
