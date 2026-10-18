package com.turnora.service;

import com.turnora.dto.AdminDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.Counter;
import com.turnora.model.entity.ServiceType;
import com.turnora.model.entity.User;
import com.turnora.model.enums.Role;
import com.turnora.model.enums.TicketStatus;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.TicketRepository;
import com.turnora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Offices (services) and staff accounts. Counters are in CounterService.
@Service
@RequiredArgsConstructor
public class AdminService {

    private final ServiceTypeRepository serviceRepository;
    private final UserRepository userRepository;
    private final CounterRepository counterRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------- Offices ----------------

    @Transactional(readOnly = true)
    public List<AdminDto.ServiceResponse> listServices(boolean onlyActive) {
        List<ServiceType> list = onlyActive
                ? serviceRepository.findByActiveTrueOrderByIdAsc()
                : serviceRepository.findAllByOrderByIdAsc();
        return list.stream().map(this::toServiceResponse).toList();
    }

    @Transactional
    public AdminDto.ServiceResponse createService(AdminDto.ServiceRequest req) {
        String name = req.name().trim();
        String prefix = req.prefix().trim().toUpperCase();

        if (serviceRepository.existsByName(name)) {
            throw AppException.conflict("An office with this name already exists");
        }
        if (serviceRepository.existsByPrefix(prefix)) {
            throw AppException.conflict("This token prefix is already used by another office");
        }

        ServiceType service = ServiceType.builder()
                .name(name)
                .prefix(prefix)
                .description(clean(req.description()))
                .avgServiceMinutes(req.avgServiceMinutes() != null ? req.avgServiceMinutes() : 5.0)
                .active(req.active() == null || req.active())
                .build();

        return toServiceResponse(serviceRepository.save(service));
    }

    @Transactional
    public AdminDto.ServiceResponse updateService(Long id, AdminDto.ServiceRequest req) {
        ServiceType service = serviceRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Office not found"));

        String name = req.name().trim();
        String prefix = req.prefix().trim().toUpperCase();

        if (!service.getName().equals(name) && serviceRepository.existsByName(name)) {
            throw AppException.conflict("An office with this name already exists");
        }
        if (!service.getPrefix().equals(prefix)) {
            if (serviceRepository.existsByPrefix(prefix)) {
                throw AppException.conflict("This token prefix is already used by another office");
            }
            if (ticketRepository.existsByServiceId(id)) {
                throw AppException.conflict("Prefix cannot be changed after tokens are created for this office");
            }
        }

        service.setName(name);
        service.setPrefix(prefix);
        service.setDescription(clean(req.description()));
        if (req.avgServiceMinutes() != null) {
            service.setAvgServiceMinutes(req.avgServiceMinutes());
        }
        if (req.active() != null) {
            service.setActive(req.active());
        }

        return toServiceResponse(serviceRepository.save(service));
    }

    private AdminDto.ServiceResponse toServiceResponse(ServiceType s) {
        long waiting = ticketRepository.countByServiceIdAndQueueDateAndStatus(
                s.getId(), LocalDate.now(), TicketStatus.WAITING);
        return new AdminDto.ServiceResponse(s.getId(), s.getName(), s.getPrefix(), s.getDescription(),
                s.getAvgServiceMinutes(), s.isActive(), waiting);
    }

    // ---------------- Staff ----------------

    @Transactional(readOnly = true)
    public List<AdminDto.StaffResponse> listStaff() {
        return userRepository.findByRoleOrderByIdAsc(Role.STAFF).stream()
                .map(this::toStaffResponse)
                .toList();
    }

    @Transactional
    public AdminDto.StaffResponse createStaff(AdminDto.StaffRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw AppException.conflict("Email already registered");
        }

        User staff = User.builder()
                .fullName(req.fullName().trim())
                .email(email)
                .phone(req.phone().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.STAFF)
                .build();

        return toStaffResponse(userRepository.save(staff));
    }

    @Transactional
    public AdminDto.StaffResponse setStaffActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Staff member not found"));
        if (user.getRole() != Role.STAFF) {
            throw AppException.badRequest("Only staff accounts can be enabled or disabled here");
        }
        user.setActive(active);
        return toStaffResponse(userRepository.save(user));
    }

    private AdminDto.StaffResponse toStaffResponse(User u) {
        Optional<Counter> counter = counterRepository.findByStaffId(u.getId());
        return new AdminDto.StaffResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.isActive(),
                counter.map(Counter::getId).orElse(null),
                counter.map(Counter::getName).orElse(null));
    }

    private String clean(String text) {
        return (text == null || text.isBlank()) ? null : text.trim();
    }
}