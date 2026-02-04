package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.common.validator.EmailValidator;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Create user use case - business logic for user creation
 * Contains only domain logic, no framework dependencies
 */
@Component
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(String username, String email, String firstName, String lastName, String password, String role,
            LocalDate dateOfBirth, Long roleId) {
        // Validation
        validateInput(username, email, firstName, lastName, password, role);
        validateEmailUniqueness(email);
        validateUsernameUniqueness(username);

        if (roleId != null && !roleRepository.existsById(roleId)) {
            throw new ValidationException("roleId", "Role not found with id: " + roleId);
        }

        // Business logic
        User user = User.builder()
                .username(username)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password(passwordEncoder.encode(password))
                .role(role != null ? role : "user")
                .dateOfBirth(dateOfBirth)
                .roleId(roleId)
                .active(true)
                .createdAt(LocalDate.now())
                .build();

        return userRepository.save(user);
    }

    private void validateInput(String username, String email, String firstName, String lastName, String password,
            String role) {
        if (username == null || username.isBlank()) {
            throw new ValidationException("username", "Username is required");
        }
        if (email == null || email.isBlank()) {
            throw new ValidationException("email", "Email is required");
        }
        if (!EmailValidator.isValid(email)) {
            throw new ValidationException("email", "Email format is invalid");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new ValidationException("firstName", "First name is required");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new ValidationException("lastName", "Last name is required");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("password", "Password must be at least 8 characters");
        }
        if (role != null && !role.matches("^(admin|user|viewer)$")) {
            throw new ValidationException("role", "Role must be one of: admin, user, viewer");
        }
    }

    private void validateEmailUniqueness(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("email", "Email already exists");
        }
    }

    private void validateUsernameUniqueness(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ValidationException("username", "Username already exists");
        }
    }
}
