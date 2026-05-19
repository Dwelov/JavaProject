package com.expensetracker.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

public class AuthDto {

    @Data
    public static class LoginRequest {
        @Email @NotBlank
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class SignupRequest {
        @NotBlank
        private String fullName;
        @Email @NotBlank
        private String email;
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters")
        private String password;
    }

    @Data
    @lombok.AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String email;
        private String fullName;
        private String message;
    }
}


