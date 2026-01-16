package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
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
 * Update role use case - business logic for role update (PATCH semantics)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateRoleUseCase {

    private final RoleRepository roleRepository;

    public Role execute(Long id, String name, String description, Set<String> permissions) {
        log.debug("Updating role with id: {}", id);

        // Find existing role
        Role existingRole = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Apply updates (PATCH semantics - only update non-null fields)
        if (name != null) {
            validateName(name);
            validateNameUniqueness(name, id);
            existingRole.setName(name.toUpperCase());
        }

        if (description != null) {
            validateDescription(description);
            existingRole.setDescription(description);
        }

        if (permissions != null) {
            validatePermissions(permissions);
            existingRole.setPermissions(permissions);
        }

        existingRole.setUpdatedAt(LocalDateTime.now());

        return roleRepository.save(existingRole);
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new ValidationException("name", "Role name cannot be blank");
        }
        if (name.length() < 3 || name.length() > 50) {
            throw new ValidationException("name", "Role name must be between 3 and 50 characters");
        }
    }

    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new ValidationException("description", "Role description cannot be blank");
        }
        if (description.length() < 10 || description.length() > 500) {
            throw new ValidationException("description", "Role description must be between 10 and 500 characters");
        }
    }

    private void validatePermissions(Set<String> permissions) {
        if (permissions.isEmpty()) {
            throw new ValidationException("permissions", "At least one permission is required");
        }

        for (String permission : permissions) {
            if (!PermissionCode.isValid(permission)) {
                throw new ValidationException("permissions", "Invalid permission code: " + permission);
            }
        }
    }

    private void validateNameUniqueness(String name, Long excludeId) {
        if (roleRepository.existsByNameAndIdNot(name.toUpperCase(), excludeId)) {
            throw new ValidationException("name", "Role name already exists");
        }
    }
}

