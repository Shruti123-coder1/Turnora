package com.turnora.service;

import com.turnora.dto.AdminDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.Counter;
import com.turnora.model.entity.ServiceType;
import com.turnora.model.entity.User;
import com.turnora.model.enums.CounterStatus;
import com.turnora.model.enums.Role;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.repository.UserRepository;
import com.turnora.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Counter management (add, edit, remove, link to offices and staff)
@Service
@RequiredArgsConstructor
public class CounterService {

    private final CounterRepository counterRepository;
    private final ServiceTypeRepository serviceRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    // Admin sees all counters. Staff sees only the counter assigned to them.
    @Transactional(readOnly = true)
    public List<AdminDto.CounterResponse> listForUser(JwtUtil.AuthUser user) {
        List<Counter> counters = user.role() == Role.ADMIN
                ? counterRepository.findAllByOrderByIdAsc()
                : counterRepository.findByStaffIdOrderByIdAsc(user.id());
        return counters.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AdminDto.CounterResponse> listAll() {
        return counterRepository.findAllByOrderByIdAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public AdminDto.CounterResponse create(AdminDto.CounterRequest req) {
        Counter counter = new Counter();
        apply(counter, req);
        return toResponse(counterRepository.save(counter));
    }

    @Transactional
    public AdminDto.CounterResponse update(Long id, AdminDto.CounterRequest req) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Counter not found"));
        apply(counter, req);
        return toResponse(counterRepository.save(counter));
    }

    @Transactional
    public void delete(Long id) {
        Counter counter = counterRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Counter not found"));
        if (counter.getCurrentTicket() != null) {
            throw AppException.conflict("This counter is serving a ticket right now");
        }
        if (ticketRepository.existsByCounterId(id)) {
            throw AppException.conflict("This counter has ticket history. Set it to OFFLINE instead of deleting.");
        }
        counterRepository.delete(counter);
    }

    // Copies the request into the counter after checking staff and offices
    private void apply(Counter counter, AdminDto.CounterRequest req) {
        String name = req.name().trim();
        boolean nameTaken = counter.getId() == null
                ? counterRepository.existsByNameIgnoreCase(name)
                : counterRepository.existsByNameIgnoreCaseAndIdNot(name, counter.getId());
        if (nameTaken) {
            throw AppException.conflict("A counter with this name already exists");
        }
        counter.setName(name);
        counter.setStatus(parseStatus(req.status()));

        // Offices
        if (req.serviceIds() == null || req.serviceIds().isEmpty()) {
            throw AppException.badRequest("Select at least one office for this counter");
        }
        Set<Long> wanted = new HashSet<>(req.serviceIds());
        List<ServiceType> found = serviceRepository.findAllById(wanted);
        if (found.size() != wanted.size()) {
            throw AppException.badRequest("Some selected offices were not found");
        }
        counter.setServices(new HashSet<>(found));

        // Staff (optional)
        if (req.staffId() == null) {
            counter.setStaff(null);
        } else {
            User staff = userRepository.findById(req.staffId())
                    .orElseThrow(() -> AppException.notFound("Staff member not found"));
            if (staff.getRole() != Role.STAFF) {
                throw AppException.badRequest("The selected user is not a staff member");
            }
            counterRepository.findByStaffId(staff.getId()).ifPresent(other -> {
                if (!other.getId().equals(counter.getId())) {
                    throw AppException.conflict("This staff member is already assigned to " + other.getName());
                }
            });
            counter.setStaff(staff);
        }
    }

    private CounterStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return CounterStatus.ACTIVE;
        }
        try {
            return CounterStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw AppException.badRequest("Status must be ACTIVE, PAUSED or OFFLINE");
        }
    }

    // Must be called inside a transaction (offices and staff are loaded lazily)
    private AdminDto.CounterResponse toResponse(Counter c) {
        List<ServiceType> offices = c.getServices().stream()
                .sorted(Comparator.comparing(ServiceType::getId))
                .toList();
        return new AdminDto.CounterResponse(
                c.getId(),
                c.getName(),
                c.getStatus().name(),
                c.getStaff() != null ? c.getStaff().getId() : null,
                c.getStaff() != null ? c.getStaff().getFullName() : null,
                offices.stream().map(ServiceType::getId).toList(),
                offices.stream().map(ServiceType::getName).toList(),
                c.getCurrentTicket() != null ? c.getCurrentTicket().getTokenNumber() : null);
    }
}
