package com.grace.gracemanageservice.domain.usecase;

import com.grace.gracemanageservice.domain.entity.Role;
import com.grace.gracemanageservice.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * List roles use case - retrieves all roles
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListRolesUseCase {

    private final RoleRepository roleRepository;

    public List<Role> execute() {
        log.debug("Listing all roles");
        return roleRepository.findAll();
    }
}

