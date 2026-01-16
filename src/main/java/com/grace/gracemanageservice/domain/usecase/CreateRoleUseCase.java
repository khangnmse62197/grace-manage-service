package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.domain.entity.PermissionCode;
import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Create role use case - business logic for role creation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateRoleUseCase {

    private final RoleRepository roleRepository;

    public Role execute(String name, String description, Set<String> permissions) {
        log.debug("Creating role with name: {}", name);

        // Validation
        validateInput(name, description, permissions);
        validateNameUniqueness(name);

        // Create role
        Role role = Role.builder()
            .name(name.toUpperCase()) // Store name in uppercase
            .description(description)
            .permissions(permissions)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return roleRepository.save(role);
    }

    private void validateInput(String name, String description, Set<String> permissions) {
        // Name validation: required, 3-50 chars
        if (name == null || name.isBlank()) {
            throw new ValidationException("name", "Role name is required");
        }
        if (name.length() < 3 || name.length() > 50) {
            throw new ValidationException("name", "Role name must be between 3 and 50 characters");
        }

        // Description validation: required, 10-500 chars
        if (description == null || description.isBlank()) {
            throw new ValidationException("description", "Role description is required");
        }
        if (description.length() < 10 || description.length() > 500) {
            throw new ValidationException("description", "Role description must be between 10 and 500 characters");
        }

        // Permissions validation: required, min 1, must be valid
        if (permissions == null || permissions.isEmpty()) {
            throw new ValidationException("permissions", "At least one permission is required");
        }

        for (String permission : permissions) {
            if (!PermissionCode.isValid(permission)) {
                throw new ValidationException("permissions", "Invalid permission code: " + permission);
            }
        }
    }

    private void validateNameUniqueness(String name) {
        if (roleRepository.existsByName(name.toUpperCase())) {
            throw new ValidationException("name", "Role name already exists");
        }
    }
}

