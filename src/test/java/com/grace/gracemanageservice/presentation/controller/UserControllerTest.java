package com.grace.gracemanageservice.presentation.controller;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.application.mapper.UserMapper;
import com.grace.gracemanageservice.application.service.UserApplicationService;
import com.grace.gracemanageservice.presentation.request.CreateUserRequest;
import com.grace.gracemanageservice.presentation.request.UpdateUserRequest;
import com.grace.gracemanageservice.presentation.response.ApiResponse;
import com.grace.gracemanageservice.presentation.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_shouldCallServiceAndReturnResponse() {
        // Arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .role("user")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .roleId(1L)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .build();

        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(userApplicationService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getRole(),
                request.getDateOfBirth(),
                request.getRoleId())).thenReturn(userDTO);

        when(userMapper.toResponse(userDTO)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> result = userController.createUser(request);

        // Assert
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(response, result.getBody().getData());

        verify(userApplicationService).createUser(
                request.getUsername(),
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getRole(),
                request.getDateOfBirth(),
                request.getRoleId());
    }

    @Test
    void updateUser_shouldCallServiceAndReturnResponse() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .lastCheckInTime(LocalDateTime.now())
                .build();

        UserDTO userDTO = UserDTO.builder()
                .id(userId)
                .firstName("Updated")
                .build();

        UserResponse response = UserResponse.builder()
                .id(userId)
                .firstName("Updated")
                .build();

        when(userApplicationService.updateUser(eq(userId), eq(request))).thenReturn(userDTO);
        when(userMapper.toResponse(userDTO)).thenReturn(response);

        // Act
        ResponseEntity<ApiResponse<UserResponse>> result = userController.updateUser(userId, request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(response, result.getBody().getData());

        verify(userApplicationService).updateUser(eq(userId), eq(request));
    }
}
