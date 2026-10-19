package com.turnora.service;

import com.turnora.dto.AuthDto;
import com.turnora.exception.AppException;
import com.turnora.model.entity.User;
import com.turnora.model.enums.Role;
import com.turnora.repository.UserRepository;
import com.turnora.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    // Student registration
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        String email = req.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw AppException.conflict("Email already registered");
        }

        User user = User.builder()
                .fullName(req.fullName().trim())
                .email(email)
                .prn(req.prn().trim())
                .phone(req.phone().trim())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.STUDENT)
                .build();
        user = userRepository.save(user);
        notificationService.notifyRole(Role.ADMIN, null, "STUDENT_REGISTERED",
                "New student account registered: " + user.getFullName() + ".");

        return new AuthDto.AuthResponse(jwtUtil.generateToken(user), toResponse(user));
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .filter(u -> u.isActive())
                .filter(u -> passwordEncoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> AppException.unauthorized("Invalid email or password"));

        return new AuthDto.AuthResponse(jwtUtil.generateToken(user), toResponse(user));
    }

    public AuthDto.UserResponse me(JwtUtil.AuthUser authUser) {
        User user = userRepository.findById(authUser.id())
                .orElseThrow(() -> AppException.unauthorized("Please login to continue"));
        return toResponse(user);
    }

    @Transactional
    public AuthDto.UserResponse updateProfile(JwtUtil.AuthUser authUser, AuthDto.UpdateProfileRequest req) {
        User user = userRepository.findById(authUser.id())
                .orElseThrow(() -> AppException.unauthorized("Please login to continue"));

        user.setFullName(req.fullName().trim());
        user.setPhone(req.phone().trim());
        if (user.getRole() == Role.STUDENT) {
            user.setPrn(req.prn() == null || req.prn().isBlank() ? null : req.prn().trim());
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(JwtUtil.AuthUser authUser, AuthDto.ChangePasswordRequest req) {
        User user = userRepository.findById(authUser.id())
                .orElseThrow(() -> AppException.unauthorized("Please login to continue"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw AppException.badRequest("Current password is wrong");
        }
        if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
            throw AppException.badRequest("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private AuthDto.UserResponse toResponse(User u) {
        return new AuthDto.UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPrn(),
                u.getPhone(), u.getRole().name());
    }
}
