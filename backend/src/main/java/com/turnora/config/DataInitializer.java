package com.turnora.config;

import com.turnora.model.entity.Counter;
import com.turnora.model.entity.ServiceType;
import com.turnora.model.entity.User;
import com.turnora.model.enums.Role;
import com.turnora.repository.CounterRepository;
import com.turnora.repository.ServiceTypeRepository;
import com.turnora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Creates the default admin, staff, college offices and counters (only if the tables are empty)
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceTypeRepository serviceRepository;
    private final CounterRepository counterRepository;
    private final PasswordEncoder passwordEncoder;

    // Change these on the deployed website using environment variables
    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    @Value("${app.seed.staff-password:Staff@123}")
    private String staffPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedServices();
        seedCounters();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(newUser("Admin", "admin@turnora.com", "9999999990", adminPassword, Role.ADMIN));
        userRepository.save(newUser("Counter Staff 1", "staff1@turnora.com", "9999999991", staffPassword, Role.STAFF));
        userRepository.save(newUser("Counter Staff 2", "staff2@turnora.com", "9999999992", staffPassword, Role.STAFF));
        log.info("Default admin and staff users created");
    }

    private User newUser(String name, String email, String phone, String password, Role role) {
        return User.builder()
                .fullName(name)
                .email(email)
                .phone(phone)
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build();
    }

    private void seedServices() {
        if (serviceRepository.count() > 0) {
            return;
        }
        serviceRepository.save(office("Accounts Section (Fee Payment)", "F", "Pay tuition or exam fee, get fee receipt", 5));
        serviceRepository.save(office("Admission and Document Verification", "A", "Verify original certificates", 8));
        serviceRepository.save(office("Exam Section", "E", "Hall ticket, marksheet, revaluation form", 6));
        serviceRepository.save(office("Scholarship Office", "S", "Scholarship forms and verification", 10));
        serviceRepository.save(office("Certificates (Bonafide, TC, ID card)", "B", "Apply for and collect certificates", 7));
        serviceRepository.save(office("Library", "L", "New card, fine payment, book issue", 4));
        serviceRepository.save(office("Hostel Office", "H", "Hostel admission, room and mess queries", 6));
        serviceRepository.save(office("Training and Placement Cell", "T", "Registration and company forms", 6));
        log.info("Default college offices created");
    }

    private ServiceType office(String name, String prefix, String description, double avgMinutes) {
        return ServiceType.builder()
                .name(name)
                .prefix(prefix)
                .description(description)
                .avgServiceMinutes(avgMinutes)
                .build();
    }

    private void seedCounters() {
        if (counterRepository.count() > 0) {
            return;
        }
        Map<String, ServiceType> byPrefix = new HashMap<>();
        serviceRepository.findAll().forEach(s -> byPrefix.put(s.getPrefix(), s));

        User staff1 = userRepository.findByEmail("staff1@turnora.com").orElse(null);
        User staff2 = userRepository.findByEmail("staff2@turnora.com").orElse(null);

        counterRepository.save(counter("Counter 1", staff1, byPrefix, "F", "S"));
        counterRepository.save(counter("Counter 2", staff2, byPrefix, "E", "B", "A"));
        counterRepository.save(counter("Counter 3", null, byPrefix, "L", "H", "T"));
        log.info("Default counters created");
    }

    private Counter counter(String name, User staff, Map<String, ServiceType> byPrefix, String... prefixes) {
        Set<ServiceType> services = new HashSet<>();
        for (String p : prefixes) {
            ServiceType s = byPrefix.get(p);
            if (s != null) {
                services.add(s);
            }
        }
        return Counter.builder()
                .name(name)
                .staff(staff)
                .services(services)
                .build();
    }
}
