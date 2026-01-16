package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.application.exception.ResourceNotFoundException;
import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Get role use case - retrieves a single role by id
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GetRoleUseCase {

    private final RoleRepository roleRepository;

    public Role execute(Long id) {
        log.debug("Getting role with id: {}", id);
        return roleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
    }
}

