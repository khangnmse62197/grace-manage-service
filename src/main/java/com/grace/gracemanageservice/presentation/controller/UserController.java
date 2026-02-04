package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.application.mapper.UserMapper;
import com.grace.gracemanageservice.application.service.UserApplicationService;
import com.grace.gracemanageservice.presentation.request.CreateUserRequest;
import com.grace.gracemanageservice.presentation.request.UpdateUserRequest;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import com.grace.gracemanageservice.presentation.response.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User REST controller - handles HTTP requests
 * Presentation layer - converts requests/responses
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserApplicationService userApplicationService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("Creating user with email: {}", request.getEmail());

        UserDTO userDTO = userApplicationService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getRole(),
                request.getDateOfBirth(),
                request.getRoleId());

        UserResponse response = userMapper.toResponse(userDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        log.info("Getting user with id: {}", id);

        UserDTO userDTO = userApplicationService.getUserById(id);
        UserResponse response = userMapper.toResponse(userDTO);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        log.info("Getting user with email: {}", email);

        UserDTO userDTO = userApplicationService.getUserByEmail(email);
        UserResponse response = userMapper.toResponse(userDTO);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<UserResponse>>> getAllUsers() {
        log.info("Getting all users");

        java.util.List<UserDTO> users = userApplicationService.getAllUsers();
        java.util.List<UserResponse> response = users.stream()
                .map(userMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);

        userApplicationService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        log.info("Updating user with id: {}", id);

        UserDTO userDTO = userApplicationService.updateUser(id, request);
        UserResponse response = userMapper.toResponse(userDTO);

        return ResponseEntity.ok(ApiResponse.success(response, "User updated successfully"));
    }
}
