package com.turnora.controller;

import com.turnora.dto.AuthDto;
import com.turnora.security.JwtUtil;
import com.turnora.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthDto.AuthResponse register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthDto.AuthResponse login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthDto.UserResponse me(@AuthenticationPrincipal JwtUtil.AuthUser user) {
        return authService.me(user);
    }

    @PutMapping("/profile")
    public AuthDto.UserResponse updateProfile(@AuthenticationPrincipal JwtUtil.AuthUser user,
                                              @Valid @RequestBody AuthDto.UpdateProfileRequest request) {
        return authService.updateProfile(user, request);
    }

    @PutMapping("/password")
    public Map<String, String> changePassword(@AuthenticationPrincipal JwtUtil.AuthUser user,
                                              @Valid @RequestBody AuthDto.ChangePasswordRequest request) {
        authService.changePassword(user, request);
        return Map.of("message", "Password changed");
    }

    // JWT is stateless: the frontend removes the token. This endpoint only confirms the logout.
    @PostMapping("/logout")
    public Map<String, String> logout() {
        return Map.of("message", "Logged out");
    }
}
