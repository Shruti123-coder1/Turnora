package com.turnora.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDto {

    public record RegisterRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name is too long")
            String fullName,

            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email")
            String email,

            @NotBlank(message = "PRN is required")
            @Size(max = 30, message = "PRN is too long")
            String prn,

            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
            String phone,

            @NotBlank(message = "Password is required")
            @Size(min = 6, max = 72, message = "Password must be 6 to 72 characters")
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "Email is required") String email,
            @NotBlank(message = "Password is required") String password) {
    }

    public record UpdateProfileRequest(
            @NotBlank(message = "Full name is required")
            @Size(max = 100, message = "Full name is too long")
            String fullName,

            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
            String phone,

            @Size(max = 30, message = "PRN is too long")
            String prn) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "Current password is required") String currentPassword,

            @NotBlank(message = "New password is required")
            @Size(min = 6, max = 72, message = "New password must be 6 to 72 characters")
            String newPassword) {
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String prn,
            String phone,
            String role) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
