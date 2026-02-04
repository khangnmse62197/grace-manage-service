package com.grace.gracemanageservice.application.service;

import com.grace.gracemanageservice.application.dto.UserDTO;
import com.grace.gracemanageservice.application.mapper.UserMapper;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import com.grace.gracemanageservice.domain.usecase.CreateUserUseCase;
import com.grace.gracemanageservice.domain.usecase.UpdateUserUseCase;
import com.grace.gracemanageservice.presentation.request.UpdateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @Test
    void createUser_shouldDelegateToUseCaseAndMapResult_WithNewFields() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String password = "StrongPassword123!";
        String role = "user";
        LocalDate dob = LocalDate.of(1990, 1, 1);
        Long roleId = 1L;

        User user = User.builder()
                .id(1L)
                .username(username)
                .email(email)
                .role(role)
                .dateOfBirth(dob)
                .roleId(roleId)
                .build();

        UserDTO userDTO = UserDTO.builder()
                .id(1L)
                .username(username)
                .email(email)
                .role(role)
                .dateOfBirth(dob)
                .roleId(roleId)
                .build();

        when(createUserUseCase.execute(username, email, firstName, lastName, password, role, dob, roleId))
                .thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        // Act
        UserDTO result = userApplicationService.createUser(username, email, firstName, lastName, password, role, dob,
                roleId);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(dob, result.getDateOfBirth());
        assertEquals(roleId, result.getRoleId());

        verify(createUserUseCase).execute(username, email, firstName, lastName, password, role, dob, roleId);
        verify(userMapper).toDTO(user);
    }

    @Test
    void updateUser_shouldDelegateToUseCaseAndMapResult() {
        // Arrange
        Long userId = 1L;
        UpdateUserRequest request = UpdateUserRequest.builder()
                .firstName("Updated")
                .dateOfBirth(LocalDate.of(1995, 5, 20))
                .roleId(2L)
                .lastCheckInTime(LocalDateTime.now())
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .firstName("Updated")
                .dateOfBirth(request.getDateOfBirth())
                .roleId(request.getRoleId())
                .lastCheckInTime(request.getLastCheckInTime())
                .build();

        UserDTO expectedDTO = UserDTO.builder()
                .id(userId)
                .firstName("Updated")
                .dateOfBirth(request.getDateOfBirth())
                .roleId(request.getRoleId())
                .lastCheckInTime(request.getLastCheckInTime())
                .build();

        when(updateUserUseCase.execute(
                eq(userId),
                eq(request.getFirstName()),
                eq(request.getLastName()),
                eq(request.getEmail()),
                eq(request.getRole()),
                eq(request.getDateOfBirth()),
                eq(request.getRoleId()),
                eq(request.getPassword()),
                eq(request.getActive()),
                eq(request.getLastCheckInTime()),
                eq(request.getLastCheckOutTime()))).thenReturn(updatedUser);

        when(userMapper.toDTO(updatedUser)).thenReturn(expectedDTO);

        // Act
        UserDTO result = userApplicationService.updateUser(userId, request);

        // Assert
        assertEquals("Updated", result.getFirstName());
        assertEquals(request.getDateOfBirth(), result.getDateOfBirth());
        assertEquals(request.getRoleId(), result.getRoleId());
        assertEquals(request.getLastCheckInTime(), result.getLastCheckInTime());

        verify(updateUserUseCase).execute(
                eq(userId),
                eq(request.getFirstName()),
                eq(request.getLastName()),
                eq(request.getEmail()),
                eq(request.getRole()),
                eq(request.getDateOfBirth()),
                eq(request.getRoleId()),
                eq(request.getPassword()),
                eq(request.getActive()),
                eq(request.getLastCheckInTime()),
                eq(request.getLastCheckOutTime()));
    }
}
