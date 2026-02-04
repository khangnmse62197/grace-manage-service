package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.grace.gracemanageservice.domain.repository.RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    @Test
    void execute_shouldUpdateFields_whenInputIsValid() {
        // Arrange
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .username("testuser")
                .email("old@example.com")
                .firstName("Old")
                .lastName("Name")
                .role("user")
                .build();

        LocalDate newDob = LocalDate.of(1995, 5, 20);
        Long newRoleId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.existsById(newRoleId)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = updateUserUseCase.execute(userId, "New", "Name", "new@example.com", "admin", newDob, newRoleId,
                null, null, null, null);

        // Assert
        assertEquals("New", result.getFirstName());
        assertEquals("Name", result.getLastName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("admin", result.getRole());
        assertEquals(newDob, result.getDateOfBirth());
        assertEquals(newRoleId, result.getRoleId());

        verify(userRepository).save(existingUser);
    }

    @Test
    void execute_shouldThrowException_whenRoleIdIsInvalid() {
        // Arrange
        Long userId = 1L;
        Long invalidRoleId = 999L;
        User existingUser = User.builder().id(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(roleRepository.existsById(invalidRoleId)).thenReturn(false);

        // Act & Assert
        assertThrows(com.grace.gracemanageservice.application.exception.ValidationException.class,
                () -> updateUserUseCase.execute(userId, null, null, null, null, null, invalidRoleId, null, null, null,
                        null));
    }
}
