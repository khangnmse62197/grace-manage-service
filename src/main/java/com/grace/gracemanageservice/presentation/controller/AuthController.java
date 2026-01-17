package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.LoginRequestDTO;
import com.grace.gracemanageservice.application.dto.LoginResponseDTO;
import com.grace.gracemanageservice.application.dto.RefreshTokenResponseDTO;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.domain.usecase.LoginUserUseCase;
import com.grace.gracemanageservice.infrastructure.security.JwtTokenProvider;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller - handles login and token refresh
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUserUseCase loginUserUseCase;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * Login endpoint - authenticates user and returns access + refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {

        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate user
        User user = loginUserUseCase.execute(request.getUsername(), request.getPassword());

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Prepare response with tokens in body (Bearer token approach)
        LoginResponseDTO loginResponse = LoginResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
            .tokenType("Bearer")
            .user(LoginResponseDTO.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .active(user.getActive())
                .build())
            .build();

        log.info("User {} logged in successfully", user.getUsername());
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }

    /**
     * Refresh endpoint - exchanges valid refresh token for new access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenResponseDTO>> refresh(
            @RequestHeader("Authorization") String authHeader) {

        log.info("Token refresh request received");

        // Extract refresh token from Bearer header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Missing or invalid Authorization header"));
        }

        String refreshToken = authHeader.substring(7);

        // Validate refresh token
        if (jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("Invalid or expired refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired refresh token"));
        }

        // Extract username and load fresh user data from DB
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
            .orElse(null);

        if (user == null || !user.isActive()) {
            log.warn("User not found or inactive: {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not found or inactive"));
        }

        // Generate new access token (refresh token stays the same)
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        RefreshTokenResponseDTO response = RefreshTokenResponseDTO.builder()
            .accessToken(newAccessToken)
            .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
            .tokenType("Bearer")
            .build();

        log.info("Token refreshed for user: {}", username);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    /**
     * Get current authenticated user info
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponseDTO.UserInfo>> getCurrentUser(
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from Bearer header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        // Validate access token
        if (!jwtTokenProvider.validateAccessToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid or expired access token"));
        }

        // Extract username and load user from DB
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
            .orElse(null);

        if (user == null || !user.isActive()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not found or inactive"));
        }

        LoginResponseDTO.UserInfo userInfo = LoginResponseDTO.UserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .active(user.getActive())
            .build();

        return ResponseEntity.ok(ApiResponse.success(userInfo, "Current user retrieved"));
    }
}

