package com.expensetracker.service;

import com.expensetracker.dto.AuthDto;
import com.expensetracker.model.User;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {

        // ✅ Trim and lowercase email before lookup
        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthDto.AuthResponse(token, user.getEmail(), user.getFullName(), "Login successful");
    }

    @Transactional
    public AuthDto.AuthResponse signup(AuthDto.SignupRequest request) {

        // 1. Clean and normalize inputs
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();

        // 2. Pre-emptive check for existing email
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RuntimeException("An account with this email already exists.");
        }

        try {
            // 3. Create and save user
            User user = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .build();

            userRepository.saveAndFlush(user);

            // 4. Generate token and return response
            String token = jwtUtil.generateToken(user.getEmail());
            return new AuthDto.AuthResponse(token, user.getEmail(), user.getFullName(), "Account created successfully");

        } catch (DataIntegrityViolationException e) {
            // 5. Handle race conditions where another thread saved the same email between step 2 and 3
            throw new RuntimeException("An account with this email already exists.");
        } catch (Exception e) {
            // 6. Generic error for everything else
            throw new RuntimeException("Could not create account: " + e.getMessage());
        }
    }
}