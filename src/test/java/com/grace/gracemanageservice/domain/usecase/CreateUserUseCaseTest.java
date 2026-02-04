package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.grace.gracemanageservice.domain.repository.RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @BeforeEach
    void setUp() {
    }

    @Test
    void execute_shouldCreateUserWithAllFields_whenInputIsValid() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String password = "StrongPassword123!";
        String role = "user";
        LocalDate dateOfBirth = LocalDate.of(1990, 1, 1);
        Long roleId = 100L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.existsById(roleId)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = createUserUseCase.execute(username, email, firstName, lastName, password, role, dateOfBirth,
                roleId);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(email, result.getEmail());
        assertEquals(firstName, result.getFirstName());
        assertEquals(lastName, result.getLastName());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals(role, result.getRole());
        assertEquals(dateOfBirth, result.getDateOfBirth());
        assertEquals(roleId, result.getRoleId());
        assertNotNull(result.getCreatedAt());
        assertTrue(result.isActive());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_shouldDefaultToUserRole_whenRoleIsNull() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String password = "StrongPassword123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User result = createUserUseCase.execute(username, email, firstName, lastName, password, null, null, null);

        // Assert
        assertEquals("user", result.getRole());
        assertNull(result.getDateOfBirth());
        assertNull(result.getRoleId());
    }

    @Test
    void execute_shouldThrowException_whenRoleIdIsInvalid() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String firstName = "Test";
        String lastName = "User";
        String password = "StrongPassword123!";
        Long invalidRoleId = 999L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(roleRepository.existsById(invalidRoleId)).thenReturn(false);

        // Act & Assert
        assertThrows(com.grace.gracemanageservice.application.exception.ValidationException.class,
                () -> createUserUseCase.execute(username, email, firstName, lastName, password, "user", null,
                        invalidRoleId));
    }
}
