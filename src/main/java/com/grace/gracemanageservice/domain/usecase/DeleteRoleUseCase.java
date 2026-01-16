package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.application.exception.ValidationException;
import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Delete role use case - business logic for role deletion
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteRoleUseCase {

    private final RoleRepository roleRepository;

    public void execute(Long id) {
        log.debug("Deleting role with id: {}", id);

        // Find existing role
        Role existingRole = roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));

        // Prevent deletion of system ADMIN role
        if (existingRole.isAdminRole()) {
            throw new ValidationException("role", "Cannot delete the system ADMIN role");
        }

        roleRepository.deleteById(id);
        log.info("Role deleted successfully with id: {}", id);
    }
}

