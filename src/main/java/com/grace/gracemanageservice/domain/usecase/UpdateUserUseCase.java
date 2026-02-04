package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.common.validator.EmailValidator;
import com.grace.gracemanageservice.domain.entity.User;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import com.grace.gracemanageservice.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateUserUseCase(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(Long id, String firstName, String lastName, String email, String role,
            LocalDate dateOfBirth, Long roleId, String password, Boolean active,
            java.time.LocalDateTime lastCheckInTime, java.time.LocalDateTime lastCheckOutTime) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Update fields if provided (not null)
        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }

        if (email != null && !email.isBlank()) {
            if (!email.equals(user.getEmail())) {
                if (userRepository.findByEmail(email).isPresent()) {
                    throw new ValidationException("email", "Email already exists");
                }
                if (!EmailValidator.isValid(email)) {
                    throw new ValidationException("email", "Email format is invalid");
                }
                user.setEmail(email);
            }
        }

        if (role != null && !role.isBlank()) {
            if (!role.matches("^(admin|user|viewer)$")) {
                throw new ValidationException("role", "Role must be one of: admin, user, viewer");
            }
            user.setRole(role);
        }

        if (dateOfBirth != null) {
            user.setDateOfBirth(dateOfBirth);
        }

        if (roleId != null) {
            if (!roleRepository.existsById(roleId)) {
                throw new ValidationException("roleId", "Role not found with id: " + roleId);
            }
            user.setRoleId(roleId);
        }

        if (lastCheckInTime != null) {
            user.setLastCheckInTime(lastCheckInTime);
        }

        if (lastCheckOutTime != null) {
            user.setLastCheckOutTime(lastCheckOutTime);
        }

        if (password != null && !password.isBlank()) {
            if (password.length() < 8) {
                throw new ValidationException("password", "Password must be at least 8 characters");
            }
            user.setPassword(passwordEncoder.encode(password));
        }

        if (active != null) {
            user.setActive(active);
        }

        return userRepository.save(user);
    }
}
