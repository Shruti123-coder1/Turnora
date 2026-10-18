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

    private AuthDto.UserResponse toResponse(User u) {
        return new AuthDto.UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPrn(),
                u.getPhone(), u.getRole().name());
    }
}
