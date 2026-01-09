package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.LoginRequestDTO;
import com.grace.gracemanageservice.application.dto.LoginResponseDTO;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.usecase.LoginUserUseCase;
import com.grace.gracemanageservice.infrastructure.security.JwtTokenProvider;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - handles login and logout
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String JWT_COOKIE_NAME = "jwt_token";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60; // 24 hours in seconds

    private final LoginUserUseCase loginUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletResponse response) {

        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate user
        User user = loginUserUseCase.execute(request.getUsername(), request.getPassword());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user);

        // Set HttpOnly cookie
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production with HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(COOKIE_MAX_AGE);
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);

        // Prepare response (without password)
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .active(user.getActive())
            .build();

        log.info("User {} logged in successfully", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        log.info("Logout request received");

        // Clear JWT cookie
        Cookie jwtCookie = new Cookie(JWT_COOKIE_NAME, null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Set to true in production
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Delete cookie
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> getCurrentUser() {
        // This endpoint will be protected by JWT filter
        // The user info can be extracted from SecurityContext
        // For now, return a placeholder - will be implemented when we add security context utilities
        return ResponseEntity.ok(ApiResponse.success(null, "Current user endpoint"));
    }
}

